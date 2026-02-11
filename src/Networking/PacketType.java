package Networking;

public enum PacketType {
    JOIN,
    DISCONNECT,
    INPUT,  //WASD
    PLAYER_STATE, //X, Y
    ATTACK_STATE,
    WORLD_STATE,
    METRICS, //latency
    LOBBY_UPDATE
    }
