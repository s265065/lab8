package lab8.ui;

import javafx.geometry.Rectangle2D;

public class AccelerationKeyframe extends SimpleKeyframe {

    private double acceleration;

    public AccelerationKeyframe(double acceleration, long time, Rectangle2D end) {
        super(time, end);

        this.acceleration = acceleration;
    }

    public AccelerationKeyframe(long time, Rectangle2D end) {
        this(9.8, time, end);
    }

    @Override
    protected double coefficient(double progress) {
        return acceleration * Math.pow(progress, 2) / 2;
    }
}
