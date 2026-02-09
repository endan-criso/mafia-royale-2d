package World;

public class World {
    public static final int TILE_SIZE = 32;
    private final byte[][] map;
    private final int width, height;

    public World(int height, int width) {
        this.height = height;
        this.width = width;
        this.map = new byte[height][width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private void buildGrass(){
        for(int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
                map[j][i] = TileType.GRASS;
            }
        }
    }

    // High-speed collision check
    public boolean isPassable(double worldX, double worldY) {
        // Convert pixel coordinate to array index
        int gridX = (int) (worldX / TILE_SIZE);
        int gridY = (int) (worldY / TILE_SIZE);

        // Map Boundary Check
        if (gridX < 0 || gridX >= width || gridY < 0 || gridY >= height) return false;

        // Data-Oriented Lookup
        byte type = map[gridY][gridX];

        // it's a WALL, you can't pass
        return type != TileType.WALL;
    }

    public boolean isInside(int y, int x){
        return y == 0 && y < height && x == 0 && x < width;
    }

    public void setTile(int y, int x, byte type) { map[y][x] = type; }
    public byte getTile(int y, int x) { return map[y][x]; }
}