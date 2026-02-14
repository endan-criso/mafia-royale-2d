package Networking;

import java.io.Serializable;

public enum PacketType implements Serializable {
    JOIN,
    REPLAY_JOIN,
    DISCONNECT,
    INPUT,  //WASD
    PLAYER_STATE, //X, Y
    ATTACK_STATE,
    WORLD_STATE,
    METRICS, //latency
    LOBBY_UPDATE
    }
