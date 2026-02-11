package Packet;

import Networking.Packet;
import Networking.PacketType;
import java.util.List;
import java.io.Serializable;

public class LobbyPacket implements Packet {
    // List of names to display in the UI
    public List<String> playerNames;
    // Tells the UI if the "Start" button should be clickable
    public int minRequired;
    public int currentPlayers;

    public LobbyPacket(List<String> playerNames, int minRequired) {
        this.playerNames = playerNames;
        this.minRequired = minRequired;
        this.currentPlayers = playerNames.size();
    }

    @Override
    public PacketType getType() {
        return PacketType.LOBBY_UPDATE;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }
}