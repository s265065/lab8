package lab8.ui;

import javafx.geometry.Rectangle2D;

public abstract class SimpleKeyframe extends AbstractKeyframe {

    private long time;

    public SimpleKeyframe(long time, Rectangle2D end) {
        super(end);

        this.time = time;
    }

    @Override
    public Rectangle2D update(Rectangle2D current, long delta) {
        super.update(current, delta);

        double coefficient = coefficient((double) lapsed / time);
        if (coefficient > 1) {
            return current.equals(end) ? null : end;
        }

        return new Rectangle2D(
                start.getMinX() + deltaX * coefficient,
                start.getMinY() + deltaY * coefficient,
                start.getWidth() + deltaWidth * coefficient,
                start.getHeight() + deltaHeight * coefficient
        );
    }

    protected abstract double coefficient(double progress);
}
