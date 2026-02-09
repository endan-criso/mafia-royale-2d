package Render;

import World.World;
import World.TileType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class WorldSection extends Canvas {

    public static final int TILE_SIZE = 32;
    private World world;

    private final Image grass;
    private final Image wall;
    //private final Image road;
    //private final Image ground;

    public WorldSection(int height, int width) {
        super(height * TILE_SIZE, width * TILE_SIZE);
        this.world = new World(width, height);

        grass = new Image(getClass().getResource("/assets/tiles/texture_16px 381.png").toExternalForm());
        wall = new Image(getClass().getResource("/assets/tiles/texture_16px 69.png").toExternalForm());
        draw();
    }

    private void draw(){
        GraphicsContext gc = getGraphicsContext2D();

        for(int i = 0; i < world.getHeight(); i++)
        {
            for (int j = 0; j < world.getWidth(); j++)
            {
                Image tileMap = null;

                switch (world.getTile(j, i))
                {
                    case TileType.GRASS -> tileMap = grass;
                    case TileType.WALL -> tileMap = wall;
                    default -> {}
                }

                gc.drawImage(tileMap, i * TILE_SIZE, j * TILE_SIZE, world.TILE_SIZE, world.TILE_SIZE );
            }
        }
    }
}
