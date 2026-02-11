package Networking;

import Logs.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import Packet.StatePacket;
import Packet.InputPacket;
import Packet.MetricsPacket;
import Player.Player;

public class Client {

    private String name;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private boolean isRunning = false;


    // Connects to the server and initializes streams
    public void connect(String host, int port, String name)throws IOException{
            this.socket = new Socket(host, port);
            this.name = name;
        // Output MUST be initialized before Input to avoid deadlock
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.isRunning = true;

        startPingThread();
        startReceiveThread();
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
                    }
                }
            } catch (Exception e) {
                Logger.get().warn("Connection Lost in startReceiveThread");
            }
            finally {
                stop();
                Logger.get().warn("Client is Shutting Down: " + name);
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public void handleState(StatePacket statePacket){return;}

    public void handleInput(InputPacket inputPacket){return;}

    public void handleMetrics(MetricsPacket metricsPacket){return;}

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

