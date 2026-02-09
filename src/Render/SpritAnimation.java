package Render;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.util.Duration;


public class SpritAnimation extends Transition {
    private final ImageView imageView;
    private final int count; //frames
    private final int offset_X;
    private final int offset_Y;

    public SpritAnimation(ImageView imageView, Duration duration, int count, int offset_X, int offset_Y)
    {
        this.imageView = imageView;
        this.count = count;
        this.offset_X = offset_X;
        this.offset_Y = offset_Y;

        setCycleDuration(duration);
        setCycleCount(Animation.INDEFINITE);
        setInterpolator(Interpolator.LINEAR);
    }

    @Override
    protected void interpolate(double k) {
        final int index = Math.min((int) Math.floor(k * count), count - 1);

        int x = index * offset_X;
        int y = 0;

        imageView.setViewport(new Rectangle2D(x, y, offset_X, offset_Y));
    }
}
