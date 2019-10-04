package lab8.ui;

import javafx.geometry.Rectangle2D;

public abstract class AbstractKeyframe implements Keyframe {

    protected Rectangle2D start, end;
    protected double deltaX, deltaY, deltaWidth, deltaHeight;
    protected long lapsed = 0;

    public AbstractKeyframe(Rectangle2D end) {
         this.end = end;
    }

    @Override
    public Rectangle2D update(Rectangle2D current, long delta) {
        if (start == null) {
            start = current;

            deltaX = end.getMinX() - start.getMinX();
            deltaY = end.getMinY() - start.getMinY();
            deltaWidth = end.getWidth() - start.getWidth();
            deltaHeight = end.getHeight() - start.getHeight();
        }

        lapsed += delta;

        return current;
    }
}
