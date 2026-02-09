package Render;

import Player.Player;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class PlayerView {

    private final SpritAnimation walkAnimation;
    private final SpritAnimation hurtAnimation;
    private final SpritAnimation runAnimation;
    private final SpritAnimation idleAnimation;
    private final SpritAnimation deadAnimation;

    private final Image idleImg;
    private final Image walkImg;
    private final Image runImg;
    private final Image hurtImg;
    private final Image deadImg;

    private final Player player;
    private final ImageView imageView;

    private PlayerState currentState = null;

    public PlayerView(Player player){
        this.player = player;

        imageView = new ImageView();

        idleImg = new Image(getClass().getResource("/assets/Character/Idle.png").toExternalForm());
        walkImg = new Image(getClass().getResource("/assets/Character/Walk.png").toExternalForm());
        runImg  = new Image(getClass().getResource("/assets/Character/Run.png").toExternalForm());
        hurtImg = new Image(getClass().getResource("/assets/Character/Hurt.png").toExternalForm());
        deadImg = new Image(getClass().getResource("/assets/Character/Dead.png").toExternalForm());

        idleAnimation = new SpritAnimation(imageView, Duration.millis(1000), 7, 128, 128);
        walkAnimation = new SpritAnimation(imageView, Duration.millis(800), 10, 128, 128);
        hurtAnimation = new SpritAnimation(imageView, Duration.millis(400), 4, 128, 128);
        deadAnimation = new SpritAnimation(imageView, Duration.millis(400), 4, 128, 128);
        runAnimation = new SpritAnimation(imageView, Duration.millis(500), 10, 128, 128);

        switchAnimation(PlayerState.IDLE, idleImg);
    }

    private void stopAll(){
        idleAnimation.stop();
        walkAnimation.stop();
        hurtAnimation.stop();
        deadAnimation.stop();
    }

    private void switchAnimation(PlayerState newState, Image img) {
        if(currentState == newState) return;

        stopAll();

        switch (newState)
        {
            case IDLE -> {
                imageView.setImage(idleImg);
                idleAnimation.play();
            }
            case RUN -> {
                imageView.setImage(runImg);
                runAnimation.play();
            }
            case WALK ->{
                imageView.setImage(walkImg);
                walkAnimation.play();
            }
            case HURT -> {
                imageView.setImage(hurtImg);
                hurtAnimation.play();
            }
            case DEAD -> {
                imageView.setImage(deadImg);
                deadAnimation.play();
            }
        }

         currentState = newState;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void render(){
        imageView.setTranslateX(player.getX());
        imageView.setTranslateY(player.getY());

        if(player.getVx() < 0) imageView.setScaleX(-1);
        else if(player.getVx() > 0) imageView.setScaleX(1);

        if(player.getVx() != 0 && player.getVy() != 0) checkWalkOrRun();
        else switchAnimation(PlayerState.IDLE, idleImg);
    }

    private void checkWalkOrRun(){
        double x = player.getVx();
        double y = player.getVy();
        if(Math.sqrt(x * x + y * y) == player.getMaxSpeed())
        {
            switchAnimation(PlayerState.RUN, runImg);
        }
        else
        {
            switchAnimation(PlayerState.WALK, walkImg);
        }
    }
}
