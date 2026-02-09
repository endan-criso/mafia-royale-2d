    package Networking;

    import Logs.Logger;

    import java.io.IOException;
    import java.io.ObjectInputStream;
    import java.io.ObjectOutputStream;
    import java.net.ServerSocket;
    import java.net.Socket;
    import Packet.InputPacket;
    import Packet.StatePacket;
    import Packet.MetricsPacket;
    import Player.Player;

    import java.util.Collection;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.concurrent.atomic.AtomicInteger;

    public class Server {

        private ServerState state = ServerState.LOBBY;
        private int maxPlayers;
        private int minPlayers = 3;
        private int teamSize = 1;

        private ServerSocket serverSocket;
        private final ConcurrentHashMap<Long, ClientHandler> clients = new ConcurrentHashMap<>();
        private final AtomicInteger nextPlayerID = new AtomicInteger(0);

        private final ConcurrentHashMap<Long, Player> players = new ConcurrentHashMap<>();
        public void start(int port) throws IOException {

            serverSocket = new ServerSocket(port);
            Logger.get().info("Server Initialized " + port);

            new Thread(this::gameLoop).start();

            //ACCEPTS CLIENT
            while(true)
            {
                Socket socket = serverSocket.accept();

                if(clients.size() >= maxPlayers){
                    socket.close();
                    continue;
                }

                long id = nextPlayerID.getAndIncrement();

                ClientHandler handler = new ClientHandler(socket, id);
                clients.put(id, handler);
                players.put(id, new Player(10,"test"));
                handler.start();
                Logger.get().info("Player " + id + " connected");
            }
        }

        private void broadcast(Packet p){
            Logger.get().info("Broadcasting: packets");

            for(ClientHandler c: clients.values())
            {
                c.send(p);
            }
        }

        private void gameLoop(){
            try {
                while (true) {
                    Thread.sleep(50); // ~20 ticks per second

                    if(state == ServerState.LOBBY){
                        if(maxPlayers >= minPlayers){
                            Logger.get().info("Server READY: Minimum players reached.");
                            state = ServerState.READY;
                        }
                    }

                    for(Player p: players.values())
                    {
                        p.update();

                        if(p.hasMoved())
                        {
                            StatePacket sp = new StatePacket();
                            sp.playerId = (int)p.getId();
                            sp.x = p.getX();
                            sp.y = p.getY();
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

        public void configure(int player, int teams){
            this.maxPlayers = Math.min(50, player); // Ensure no more than 50
            this.teamSize = teams;
            Logger.get().info("Server Configured: Max " + maxPlayers + ", Teams of " + teamSize);
        }

        public void closeServer(){
            if(serverSocket != null && !serverSocket.isClosed())
            {
                try {
                    serverSocket.close();
                } catch (IOException e) {}
            }

            for(ClientHandler c: clients.values())
            {
                c.shutdown();
            }

            clients.clear();
            players.clear();
        }

    class ClientHandler extends Thread{
        ObjectInputStream in;
        ObjectOutputStream out;
        Long playerId;
        private Socket socket;
        private boolean running;

        ClientHandler(Socket socket, Long id) throws IOException{
            this.playerId = id;
            this.socket = socket;
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }

        public void send(Packet p){
            try {
                out.writeObject(p);
                out.reset();
                out.flush();
            } catch (IOException e){
                Logger.get().error("FAILED SEND PACKET TYPE");
            }
        }

        public void run(){
            try {
                while (true){
                    Object obj = (Packet) in.readObject();

                    if(obj instanceof MetricsPacket)
                    {
                        MetricsPacket mp = (MetricsPacket) obj;
                        if(mp.ping){
                            mp.pong = false;
                            mp.ping = true;
                        }
                            send(mp);
                    }

                    if(obj instanceof InputPacket)
                    {
                        InputPacket ip = (InputPacket) obj;
                        Player player = players.get(playerId);

                        if(player != null)
                        {
                            double speed = 2.5;
                            if (ip.up)    player.setVy(player.getVy() - speed);
                            if (ip.down)  player.setVy(player.getVy() + speed);
                            if (ip.left)  player.setVx(player.getVx() - speed);
                            if (ip.right) player.setVx(player.getVx() + speed);
                        }
                    }
                }
            } catch (Exception e){
                Logger.get().warn("Player " + playerId + " disconnected");
            }
            finally {
                shutdown();
                Logger.get().warn("Server Shutting Down");
            }
        }

        private void shutdown(){
            running = false;
            clients.remove(playerId);
            players.remove(playerId);
            closeResource();
        }

        private void closeResource(){
            try {in.close();} catch (IOException e) {}
            try {out.close();} catch (IOException e) {}
            try {socket.close();} catch (IOException e) {}
        }
    }
        // Get all players currently connected
        public Collection<Player> getAllPlayers() {
            return players.values();
        }

        // Get a specific player by their ID
        public Player getPlayer(long id) {
            return players.get(id);
        }
    }
