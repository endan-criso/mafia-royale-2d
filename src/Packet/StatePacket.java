package Packet;

import Networking.Packet;
import Networking.PacketType;

public class StatePacket implements Packet {
    public int playerId;
    public double x, y;
    public int health;
    public int energy;

    @Override
    public PacketType getType() {
        return PacketType.PLAYER_STATE;
    }
}
