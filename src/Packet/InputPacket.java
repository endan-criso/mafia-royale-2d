package Packet;

import Networking.Packet;
import Networking.PacketType;

public class InputPacket implements Packet {
    public boolean up;
    public boolean down;
    public boolean left;
    public boolean right;

    public boolean pickUpItems;
    public boolean switchGun;
    public boolean throwAction;

    public boolean reload;
    public boolean attackOrShoot;

    @Override
    public PacketType getType() {
        return PacketType.INPUT;
    }
}
