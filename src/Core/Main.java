package Core;

import Player.Player;
import Render.MenuSection;
import Render.WorldSection;
import Utils.SoundManager;
import World.World;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    static final int WORLD_HEIGHT = 20; // Reduced for visibility
    static final int WORLD_WIDTH = 20;
    static final int TILE_SIZE = 30;


    // These will eventually come from your StatePackets!
    double playerX = 50;
    double playerY = 50;

    @Override
    public void start(Stage stage) {

        //Menu Section
        MenuSection menu = new MenuSection(stage, getHostServices());

        //start sound
        SoundManager.init();
        //SoundManager.playIntro();

        // Generate the map
        //WorldSection worldSection = new WorldSection(100, 100);
        Group root = new Group();
        //root.getChildren().add();

        Player playerLogic = new Player(1, "Player1");
        playerLogic.setX(100); // Starting position
        playerLogic.setY(100);

        Render.PlayerView playerView = new Render.PlayerView(playerLogic);

        // 4. Add the character's image to the screen
        // We get the ImageView from the PlayerView class you wrote
        //root.getChildren().add(playerView.getImageView());

        Scene scene = new Scene(menu.getRoot());
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}