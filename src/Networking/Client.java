package Networking;

import Logs.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


import Packet.StatePacket;
import Packet.InputPacket;
import Packet.MetricsPacket;
import Packet.LobbyPacket;
import Player.Player;
import Render.MenuSection;

public class Client {

    private String name;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private BlockingQueue<Packet> incoming;
    public boolean isRunning = false;

    //Menu Section
    private MenuSection menuSection;


    // Connects to the server and initializes streams
    public void connect(String host, int port, String name)throws IOException{
            this.socket = new Socket(host, port);
            this.name = name;
        // Output MUST be initialized before Input to avoid deadlock
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        incoming = new LinkedBlockingQueue<>(1024);
        this.isRunning = true;

        //Send Player name
        LobbyPacket lp = new LobbyPacket(PacketType.JOIN, name);
        out.writeObject(lp);
        out.flush();
        Logger.get().info("CLIENT SEND THE JOIN REQUEST");

        startPingThread();
        startReceiveThread();
    }

    public void setMenu(MenuSection menuSection)
    {
        this.menuSection = menuSection;
    }

    private void startReceiveThread(){
        Thread receiveThread = new Thread(() -> {
            try {
                while(isRunning){
                    Object obj = in.readObject();

                    if(obj instanceof StatePacket)
                    {
                        StatePacket sp = (StatePacket)obj;
                        handleState(sp);
                    } else if (obj instanceof InputPacket) {
                        InputPacket ip = (InputPacket)obj;
                        handleInput(ip);
                    } else if (obj instanceof MetricsPacket) {
                        MetricsPacket mp = (MetricsPacket) obj;
                        handleMetrics(mp);
                    } else if(obj instanceof LobbyPacket){
                        LobbyPacket lp = (LobbyPacket) obj;
                        handleLobby(lp);
                    }
                }
            } catch (SocketException | EOFException e) {
                Logger.get().info("Server closed connection: " + e.getMessage());
                stop();
            } catch (Exception e) {
                Logger.get().error("Receive error (not disconnect)");
                stop();
                throw new RuntimeException(e);
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public void handleState(StatePacket statePacket){return;}

    public void handleInput(InputPacket inputPacket){return;}

    public void handleMetrics(MetricsPacket metricsPacket){return;}

    public void handleLobby(LobbyPacket lobbyPacket){
        System.out.println("Received Lobby Update! Players: " + lobbyPacket.getPlayerNames().size());
        if(menuSection != null) {
            javafx.application.Platform.runLater(() -> {
                menuSection.updateLobbyUI(lobbyPacket);
            });
        }
    }

    private void startPingThread(){
        Thread pingThread = new Thread(() -> {
            try{
                while (isRunning){
                    MetricsPacket mp = new MetricsPacket();
                    mp.ping = true;
                    mp.timestamp = (int) System.currentTimeMillis();
                    send(mp);
                    Thread.sleep(2000);
                }
            } catch (Exception e) {}
        });
        pingThread.setDaemon(true);
        pingThread.start();
    }

    public void stop(){
        try {
            isRunning = false;
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(Packet packet){
        try{
            out.writeObject(packet);
            out.reset();
            out.flush();  // CRITICAL: Forces the data out to the network immediately
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

