package Packet;

import Networking.Packet;
import Networking.PacketType;
import Networking.ServerState;

import java.util.List;
import java.io.Serializable;

public class LobbyPacket implements Packet {
    // List of names to display in the UI
    public PacketType type; // Set this to "JOIN_REQUEST"
    public String singleName; // The name of the player joining
    public List<String> playerNames;
    public ServerState state;
    // Tells the UI if the "Start" button should be clickable
    public int minRequired;

    // For JOIN requests
    public LobbyPacket(PacketType type, String singleName){
        this.type = type;
        this.singleName = singleName;

    }

    // For LOBBY updates from Server
    public LobbyPacket(PacketType type, List<String> playerNames, int minRequired, ServerState state) {
        this.type = type;
        this.playerNames = playerNames;
        this.minRequired = minRequired;
        this.state = state;
    }

    @Override
    public PacketType getType() {
        return type;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }
}