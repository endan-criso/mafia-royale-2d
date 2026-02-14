    package Networking;

    import Logs.Logger;

    import java.io.IOException;
    import java.io.ObjectInputStream;
    import java.io.ObjectOutputStream;
    import java.net.*;

    import Packet.InputPacket;
    import Packet.StatePacket;
    import Packet.MetricsPacket;
    import Packet.JoinPacket;
    import Packet.LobbyPacket;
    import Packet.ClosingPacket;
    import Player.Player;

    import java.util.List;
    import java.util.concurrent.BlockingQueue;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.concurrent.LinkedBlockingQueue;
    import java.util.concurrent.atomic.AtomicInteger;

    public class Server {

        //PORT
        private static final int GAME_PORT = 9173;
        private static final int DISCOVERY_PORT = 8888;


        private ServerState state = ServerState.LOBBY;
        private int maxPlayers;
        private int minPlayers = 3;
        private int teamSize = 1;

        //host
        private Long host = null;

        //Thread
        private boolean running = false;
        private volatile boolean lobbyRunning = false;
        private volatile boolean broadcastSearch = false;
        private volatile boolean clientsName = false;

        private ServerSocket serverSocket;
        private final ConcurrentHashMap<Long, ClientHandler> clients = new ConcurrentHashMap<>();
        private final AtomicInteger nextPlayerID = new AtomicInteger(0);
        private final ConcurrentHashMap<Long, Player> players = new ConcurrentHashMap<>();

        public void start(int port) throws IOException {

            serverSocket = new ServerSocket(port);
            running = true;
            broadcastSearch = true;
            clientsName = true;

            Logger.get().info("Server Initialized " + port);
            new Thread(this::lobby).start();

            //ACCEPTS CLIENT
            while (running) {
                Socket socket = serverSocket.accept();

                if (clients.size() >= maxPlayers) {
                    socket.close();
                    continue;
                }

                long id = nextPlayerID.getAndIncrement();

                ClientHandler handler = new ClientHandler(socket, id); //Add the players here
                clients.put(id, handler);
                handler.start();
                Logger.get().info("Player " + id + " connected");
                Logger.get().info("ClientHandler Thread: " + handler.getState());
            }
        }

        public void startDiscoveryBroadcast(){
            Thread broadcastDiscovery = new Thread(() -> {
                try(DatagramSocket socket = new DatagramSocket()) {

                    socket.setBroadcast(true);

                    while (broadcastSearch)
                    {
                        String msg = "BATTLE_MAFIA_SERVER:" + GAME_PORT;
                        byte[] data = msg.getBytes();

                        DatagramPacket packet = new DatagramPacket(
                                data,
                                data.length,
                                InetAddress.getByName("255.255.255.255"),
                                DISCOVERY_PORT
                        );

                        socket.send(packet);

                        Thread.sleep(2000);

                    }
                } catch (Exception e) {
                    Logger.get().error("FAILED TO START: " + Thread.currentThread());
                }
            });
            broadcastDiscovery.start();
        }

        private void broadcast(Packet p) {

            for (ClientHandler c : clients.values()) {
                if(c != null && c.isAlive()) c.send(p);
            }
        }

        private void sendClientsNameBroadcaster(){
            Thread sendClientsName = new Thread(() -> {
               try{
                   while (clientsName)
                   {
                       List<String> names = players.values().stream().map(Player::getName).toList();
                       LobbyPacket lp = new LobbyPacket(PacketType.LOBBY_UPDATE ,names, this.minPlayers, this.state, this.host);
                       broadcast(lp);
                       Thread.sleep(1000);
                   }
               }catch (Exception e)
               {
                   Logger.get().error(Thread.currentThread().getName()+ " is forced stopped");
               }
            });
            sendClientsName.setName("Client Name Broadcaster");
            sendClientsName.setDaemon(true);
            sendClientsName.start();
        }

        private void lobby() {
            try {
                Thread.currentThread().setName("LOBBY");
                Logger.get().warn(Thread.currentThread().getName() + " started successfully");
                while (lobbyRunning && running) {
                    Thread.sleep(150);

                    if (clients.size() >= minPlayers) {
                        state = ServerState.READY;
                    }

                    if (state == ServerState.STARTING) {
                        new Thread(this::gameLoop).start();
                        return;
                    }
                }
            } catch (InterruptedException e) {
                Logger.get().error(Thread.currentThread().getName() + "Failed to start: " + Thread.currentThread().getState());
            }
        }

        private void stopClientAccepting(){
            try{
                serverSocket.close();
                Logger.get().info("CLOSED THE CLIENT ACCEPTING " + this);
                return;
            } catch (IOException e) {
                Logger.get().error("stopClient in GameLoop failed");

            }
        }

        private void gameLoop() {
            try {
                Thread.currentThread().setName("GAME LOOP");
                Logger.get().warn(Thread.currentThread().getName() + " started successfully");
                lobbyRunning = false;
                broadcastSearch = false;
                clientsName = false;
                stopClientAccepting();
                while (true) {
                    Thread.sleep(50); // ~20 ticks per second


                    for (Player p : players.values()) {
                        p.update();

                        if (p.hasMoved()) {
                            StatePacket sp = new StatePacket();
                            sp.playerId = (int) p.getId();
                            sp.x = p.getX();
                            sp.y = p.getY();
                            Logger.get().info("Broadcasting: packets");
                            broadcast(sp);
                            p.markPosition();
                        }
                    }
                }
            } catch (Exception e) {
                Logger.get().error("GameLoop in Server Fails");
                throw new RuntimeException(e);
            }
        }

        public void configure(int player, int teams) {
            this.maxPlayers = Math.min(50, player); // Ensure no more than 50
            this.teamSize = teams;
            Logger.get().info("Server Configured: Max " + maxPlayers + ", Teams of " + teamSize);
        }

        public void closeServer() {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    running = false;
                    serverSocket.close();
                } catch (IOException e) {
                }
            }

            for (ClientHandler c : clients.values()) {
                c.shutdown();
            }

            clients.clear();
            players.clear();
        }

        class ClientHandler extends Thread {
            private ObjectInputStream in;
            private ObjectOutputStream out;
            private Long playerId;
            private Socket socket;
            private boolean running;
            private final BlockingQueue<Packet> outgoing;

            ClientHandler(Socket socket, Long id) throws IOException {
                this.playerId = id;
                this.socket = socket;
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                running = true;
                outgoing = new LinkedBlockingQueue<>(1024);
            }

            public void send(Packet p) {
                if(running) outgoing.offer(p);
            }

            private void writerThread(){
                try {
                    Logger.get().info("STARTED: " + Thread.currentThread().getName());
                    while (running && !socket.isClosed())
                    {
                        Packet packet = outgoing.take();

                        out.writeObject(packet);
                        out.flush();
                        out.reset();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public void run() {
                try {

                    Object join = in.readObject();
                    if (join instanceof JoinPacket joinPacket && joinPacket.type == PacketType.JOIN) {
                        Player player = new Player(playerId, joinPacket.singleName);
                        players.put(playerId, player);

                        JoinPacket jp = new JoinPacket(PacketType.REPLAY_JOIN, playerId);
                        send(jp);  // Send response packet
                        Logger.get().info("CLIENT RESPOND TO JOIN");

                        //Set the host
                        if (host == null) {
                            host = playerId;
                        }

                        //start lobby
                        if (!lobbyRunning)
                        {
                            lobbyRunning = true;
                            sendClientsNameBroadcaster();
                            startDiscoveryBroadcast();
                            new Thread(Server.this::lobby, "LOBBY").start();

                        }

                        Thread writer = new Thread(this::writerThread, "Writer-" + playerId);
                        writer.setDaemon(true);
                        writer.start();
                    }
                    else
                    {
                        shutdown();
                        return;
                    }


                    while (running && !socket.isClosed()) {
                        Object obj = in.readObject();

                        if (obj instanceof MetricsPacket) {
                            MetricsPacket mp = (MetricsPacket) obj;
                            if (mp.ping) {
                                mp.pong = true;
                                mp.ping = false;
                            }
                            send(mp);
                        }

                        if (obj instanceof InputPacket) {
                            InputPacket ip = (InputPacket) obj;
                            Player player = players.get(playerId);

                            if (player != null) {
                                double speed = 2.5;
                                if (ip.up) player.setVy(player.getVy() - speed);
                                if (ip.down) player.setVy(player.getVy() + speed);
                                if (ip.left) player.setVx(player.getVx() - speed);
                                if (ip.right) player.setVx(player.getVx() + speed);
                            }
                        }

                        if(obj instanceof ClosingPacket cp)
                        {
                            if(cp.type == PacketType.DISCONNECT)
                            {
                                shutdown();
                            }
                        }

                    }
                } catch (Exception e) {
                    Logger.get().warn("Player " + playerId + " disconnected");
                } finally {
                    shutdown();
                    Logger.get().warn("Server Shutting Down");
                }
            }

            private void shutdown() {
                running = false;
                clients.remove(playerId);
                players.remove(playerId);
                closeResource();
            }

            private void closeResource() {
                try {
                    in.close();
                } catch (IOException e) {
                }
                try {
                    out.close();
                } catch (IOException e) {
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
