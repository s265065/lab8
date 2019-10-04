package lab8.ui;

import javafx.geometry.Rectangle2D;

public class PowerKeyframe extends SimpleKeyframe {

    private double power;

    public PowerKeyframe(double power, long time, Rectangle2D end) {
        super(time, end);

        this.power = power;
    }

    public PowerKeyframe(long time, Rectangle2D end) {
        this(Math.E, time, end);
    }

    @Override
    protected double coefficient(double progress) {
        return Math.pow(progress, power);
    }
}
