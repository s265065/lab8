package lab8.ui;

import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import lab8.previous.Hat;

import java.util.*;

public class HatCanvas extends Canvas {

    private static final int AREA_SIZE = 1000;
    private static final int SHELF_HEIGHT = AREA_SIZE / 6;

    private ObservableList<Hat> target;
    private final ArrayList<HatSprite> sprites = new ArrayList<>();
    private double[] widths = new double[] { 0, 0, 0, 0, 0, 0 };
    private Hat selected = null;

    private HatSelectingListener listener = model -> { };

    private static Image wallpaper;

    private AnimationTimer timer = new AnimationTimer() {

        private boolean initialization = true;
        private long previous = -1;

        @Override
        public void handle(long now) {
            sprites.removeIf(sprite -> target.parallelStream().noneMatch(hat -> hat.getId() == sprite.origin.getId()));

            double[] currentX = new double[6];
            for (Hat hat : target) {
                HatSprite sprite = sprites.parallelStream()
                        .filter(s -> s.origin.getId() == hat.getId())
                        .findFirst().orElse(null);

                if (sprite == null) {
                    sprites.add(sprite = new HatSprite(hat, currentX[hat.getShelf() % 6], !initialization));
                } else {
                    if (sprite.origin != hat) {
                        sprite.origin = hat;
                    }

                    if (sprite.keyframes.isEmpty()) {
                        Rectangle2D rightVisualRectangle = sprite.rightVisualRectangle(currentX[hat.getShelf() % 6]);

                        if (!sprite.visualRect.equals(rightVisualRectangle)) {
                            double flyingY = Math.min(sprite.visualRect.getMinY(), rightVisualRectangle.getMinY()) - 20;

                            sprite.keyframes.add(new LinearKeyframe(500_000_000, new Rectangle2D(
                                    sprite.visualRect.getMinX(),
                                    flyingY,
                                    sprite.visualRect.getWidth(),
                                    sprite.visualRect.getHeight()
                            )));

                            sprite.keyframes.add(new LinearKeyframe(500_000_000, new Rectangle2D(
                                    rightVisualRectangle.getMinX(),
                                    flyingY,
                                    rightVisualRectangle.getWidth(),
                                    rightVisualRectangle.getHeight()
                            )));

                            sprite.keyframes.add(new AccelerationKeyframe(500_000_000, rightVisualRectangle));
                        }
                    }
                }

                currentX[hat.getShelf() % 6] += sprite.visualRect.getWidth();
            }

            if (previous != -1) {
                update(now - previous);
            }

            previous = now;
            widths = currentX;

            draw();
            if (initialization) {
                initialization = false;
            }
        }
    };

    static {
        try {
            wallpaper = new Image(HatCanvas.class.getResourceAsStream("/img/wallpaper.png"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        sprites.clear();
    }

    public void setTarget(ObservableList<Hat> target) {
        this.target = target;

        System.out.println(this.target);

        selected = null;
        sprites.clear();

        timer.start();

        setOnMouseClicked(e -> onClicked(e.getX(), e.getY()));
    }

    public void selectHat(Hat model) {
        selected = model;
    }

    public void setSelectingListener(HatSelectingListener listener) {
        this.listener = listener;
    }

    private void onClicked(double x, double y) {
        Hat newSelected = null;

        for (HatSprite current : sprites) {
            if (current.contains(x, y)) {
                newSelected = current.origin;
            }
        }

        if (selected != newSelected) {
            listener.selected(selected = newSelected);
        }
    }

    private void update(long delta) {
        for (HatSprite sprite : sprites) {
            sprite.update(delta);
        }
    }

    private void draw() {
        GraphicsContext context = getGraphicsContext2D();
        context.clearRect(0, 0, getWidth(), getHeight());
        context.save();

        double scale = getScale();
        Point2D translate = getTranslation();

        context.scale(scale, scale);
        context.translate(translate.getX(), translate.getY());

        if (wallpaper != null) {
            double wZoom = Math.max(getWidth() / wallpaper.getWidth(), getHeight() / wallpaper.getHeight());
            double wWidth = wallpaper.getWidth() * wZoom;
            double wHeight = wallpaper.getHeight() * wZoom;

            context.setFill(new ImagePattern(wallpaper, (getWidth() - wWidth) / 2, (getHeight() - wHeight) / 2,
                    wWidth, wHeight, false));

            context.fillRect(0, 0, AREA_SIZE, AREA_SIZE);
        }

        context.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.BLACK), new Stop(1, Color.TRANSPARENT)));
        for (int i = 1; i < 6; ++i) {
            context.fillRect(0, getShelfY(i), AREA_SIZE, SHELF_HEIGHT * 0.6);
        }

        context.setStroke(Color.BLACK);
        context.setLineWidth(2);
        context.strokePolygon(
                new double[] { 0, AREA_SIZE, AREA_SIZE, 0, 0 },
                new double[] { 0, 0, AREA_SIZE, AREA_SIZE, 0 },
                5
        );

        context.setFill(Color.BROWN.darker());
        for (int i = 1; i < 6; ++i) {
            context.fillRect(0, getShelfY(i) - SHELF_HEIGHT * 0.4,
                    AREA_SIZE, SHELF_HEIGHT * 0.4);
        }

        context.setFill(Color.BROWN);
        for (int i = 1; i < 6; ++i) {
            context.fillRect(0, getShelfY(i), AREA_SIZE, 3);
        }

        sprites.forEach((b) -> b.draw(context));
        context.restore();
    }

    private double getScale() {
        return Math.min(getWidth(), getHeight()) / AREA_SIZE;
    }

    private static double getShelfY(int shelf) {
        return (shelf + 0.25) * SHELF_HEIGHT;
    }

    private Point2D getTranslation() {
        double scale = getScale();

        if (getWidth() < getHeight())
            return new Point2D(0, (getHeight() - AREA_SIZE * scale) / 2);
        else
            return new Point2D((getWidth() - AREA_SIZE * scale) / 2, 0);
    }

    private class HatSprite {

        private Hat origin;

        private Rectangle2D visualRect;
        private Queue<Keyframe> keyframes = new LinkedList<>();

        private HatSprite(Hat origin, double visualX, boolean anim) {
            this.origin = origin;

            Rectangle2D rightVisualRect = rightVisualRectangle(visualX);

            if (anim) {
                visualRect = new Rectangle2D(-rightVisualRect.getWidth(), rightVisualRect.getHeight() * getShelfScale(),
                        rightVisualRect.getWidth(), rightVisualRect.getHeight());

                keyframes.add(new PowerKeyframe(500_000_000, new Rectangle2D(rightVisualRect.getMinX(),
                        this.visualRect.getMinY(), rightVisualRect.getWidth(), rightVisualRect.getHeight())));
                keyframes.add(new AccelerationKeyframe(500_000_000, new Rectangle2D(rightVisualRect.getMinX(),
                        rightVisualRect.getMinY(), rightVisualRect.getWidth(), rightVisualRect.getHeight())));
            } else {
                visualRect = rightVisualRect;
            }
        }

        private Rectangle2D rightVisualRectangle(double visualX) {
            double width = 70 + origin.getSize() * 8;

            return new Rectangle2D(visualX, getShelfY(origin.getShelf()) - 2, width, width * 8 / 7);
        }

        private void draw(GraphicsContext context) {
            context.save();

            Color color = Color.web(origin.getColor());
            Color userColor = Color.web(origin.getUserColor());

            context.setStroke(Color.BLACK);
            context.setLineWidth(1);
            context.setFill(color);

            double scale = getShelfScale();
            double x = visualRect.getMinX() * scale;
            double y = visualRect.getMinY() - visualRect.getHeight() * scale;
            double width = visualRect.getWidth() * scale;
            double height = visualRect.getHeight() * scale;

            double bottomY = y + height * 0.8;
            double bottomHeight = height * 0.2;
            double baseX = x + width * 0.2;
            double baseY = y + width * 0.2;
            double baseWidth = width * 0.6;
            double baseHeight = height * 0.7;
            double ribbonY = y + height * 0.5;
            double ribbonCoverY = y + height * 0.4;
            double baseCoverHeight = height * 0.3;
            double topHeight = height * 0.4;

            double holeX = x + width * 0.21;
            double holeY = y + height * 0.1;
            double holeWidth = width * 0.58;
            double holeHeight = height * 0.2;

            context.fillOval(baseX, bottomY, baseWidth, bottomHeight);
            context.strokeOval(baseX, bottomY, baseWidth, bottomHeight);
            context.fillRect(baseX - 1, baseY - 1, baseWidth + 1, baseHeight + 1);

            context.setFill(userColor);
            context.fillOval(baseX, ribbonY, baseWidth, bottomHeight);
            context.strokeOval(baseX, ribbonY, baseWidth, bottomHeight);
            context.fillRect(baseX - 1, ribbonY - 1, baseWidth + 1, bottomHeight / 2 + 1);

            context.setFill(color);
            context.fillOval(baseX, ribbonCoverY, baseWidth, bottomHeight);
            context.strokeOval(baseX, ribbonCoverY, baseWidth, bottomHeight);

            context.fillRect(baseX - 1, baseY - 1, baseWidth + 1, baseCoverHeight + 1);
            context.strokeLine(baseX, baseY, baseX, baseY + baseHeight);
            context.strokeLine(baseX + baseWidth, baseY, baseX + baseWidth, baseY + baseHeight);
            context.fillOval(x, y, width, topHeight);
            context.strokeOval(x, y, width, topHeight);

            context.setFill(color.darker());
            context.fillOval(holeX, holeY, holeWidth, holeHeight);
            context.strokeOval(holeX, holeY, holeWidth, holeHeight);

            context.restore();

            if (selected != null && selected.getId() == origin.getId()) {
                drawSelectionOutline(context);
            }
        }

        private void drawSelectionOutline(GraphicsContext context) {
            context.save();

            Color color = Color.rgb(255, 154, 0, .75);

            context.setLineWidth(4);
            context.setStroke(color);
            context.setLineDashes(10);
            context.setLineDashOffset(10);
            double scale = getShelfScale();
            context.strokeRect(visualRect.getMinX() * scale,
                    visualRect.getMinY() - visualRect.getHeight() * scale,
                    visualRect.getWidth() * scale, visualRect.getHeight() * scale);

            context.restore();
        }

        private double getShelfScale() {
            return Math.min(AREA_SIZE / widths[origin.getShelf() % 6], 1);
        }

        private void update(long delta) {
            Keyframe keyframe = keyframes.peek();

            if (keyframe != null) {
                Rectangle2D newVisualRect = keyframe.update(visualRect, delta);

                if (newVisualRect == null) {
                    keyframes.poll();
                } else {
                    visualRect = newVisualRect;
                }
            }
        }

        private boolean contains(double x, double y) {
            double shelfScale = getShelfScale();
            Point2D translation = getTranslation();
            double scale = getScale();

            return new Rectangle2D(
                    (visualRect.getMinX() * scale + translation.getX()) * shelfScale,
                    visualRect.getMinY() * scale + translation.getY() -
                            (visualRect.getHeight() * scale + translation.getY()) * shelfScale,
                    (visualRect.getWidth() * scale + translation.getX()) * shelfScale,
                    (visualRect.getHeight() * scale + translation.getY()) * shelfScale
            ).contains(x, y);
        }
    }
}
