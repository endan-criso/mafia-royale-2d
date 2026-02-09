package Loot;

public class Items {
    private int instanceId;
    private int typeID;
    private int x, y;

    public Items(int instanceId, int typeID, int x, int y) {
        this.instanceId = instanceId;
        this.typeID = typeID;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return instanceId;
    }

    public void setId(int instanceId) {
        this.instanceId = instanceId;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }
}
