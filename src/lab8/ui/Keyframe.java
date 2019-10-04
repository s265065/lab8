package lab8.ui;

import javafx.geometry.Rectangle2D;

public interface Keyframe {

    public Rectangle2D update(Rectangle2D current, long delta);
}
