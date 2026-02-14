package Packet;

import Networking.Packet;
import Networking.PacketType;
import Networking.ServerState;

import java.util.List;
import java.io.Serializable;

public class LobbyPacket implements Packet {
    // List of names to display in the UI

    public List<String> playerNames;
    public ServerState state;
    public Long host;
    // Tells the UI if the "Start" button should be clickable
    public int minRequired;



    // For LOBBY updates from Server
    public LobbyPacket(PacketType type, List<String> playerNames, int minRequired, ServerState state, Long host) {
        this.playerNames = playerNames;
        this.minRequired = minRequired;
        this.state = state;
        this.host = host;
    }

    @Override
    public PacketType getType() {
        return PacketType.LOBBY_UPDATE;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }
}