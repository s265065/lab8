package lab8.ui;

import javafx.geometry.Rectangle2D;

public class LinearKeyframe extends SimpleKeyframe {

    public LinearKeyframe(long time, Rectangle2D end) {
        super(time, end);
    }

    @Override
    protected double coefficient(double progress) {
        return progress;
    }
}
