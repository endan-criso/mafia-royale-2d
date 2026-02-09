package Loot;

public enum ItemType {

    MEDIKIT(0), WATER(1), AMMO_AR_SMG(2), AMMO_PISTOL(3), SHOTGUN(4);

    private final int id;
    ItemType(int id){this.id = id;}

    public int getId() {
        return id;
    }
}
