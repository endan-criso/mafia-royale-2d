package Packet;

import Networking.Packet;
import Networking.PacketType;

public class MetricsPacket implements Packet {
    public int timestamp;
    public boolean ping;
    public boolean pong;

    @Override
    public PacketType getType(){
        return PacketType.METRICS;
    }
}
