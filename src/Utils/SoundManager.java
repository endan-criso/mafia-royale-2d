package Utils;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public final class SoundManager {

    private static MediaPlayer musicPlayer;

    public static AudioClip punch1;
    public static AudioClip punch2;
    public static AudioClip throwHit;
    public static AudioClip wallBreak;

    public static void init() {
        punch1    = load("/sounds/punch-1.mp3");
        punch2    = load("/sounds/punch-2.mp3");
        throwHit  = load("/sounds/body-fall.mp3");
        wallBreak = load("/sounds/wall-break.mp3");
    }

    private static AudioClip load(String path) {
        URL url = SoundManager.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("Missing sound: " + path);
        }
        return new AudioClip(url.toExternalForm());
    }

    public static void playIntro(){

        if(musicPlayer != null){
            musicPlayer.stop();
        }

        URL url = SoundManager.class.getResource("/sounds/game-intro.mp3");

        if(url != null)
        {
            Media media = new Media(url.toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(0.5);
            musicPlayer.play();

        }
    }

    public void stopIntro(){
        if(musicPlayer != null) musicPlayer.stop();
    }
}
