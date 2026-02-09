package Networking;

import java.io.Serializable;

public interface Packet extends Serializable {
    PacketType getType();
}

