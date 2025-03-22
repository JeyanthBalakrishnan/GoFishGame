import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class AnimatedBackground extends JPanel {
    private final java.util.List<Bubble> bubbles;
    private Timer animationTimer;
    private final Random random;
    private final Color backgroundColor;
    private final int numBubbles = 20;

    private class Bubble {
        double x, y;
        double speed;
        double size;
        double opacity;
        Color color;

        Bubble() {
            reset();
        }

        void reset() {
            x = random.nextDouble() * getWidth();
            y = getHeight() + random.nextDouble() * 50;
            speed = 1 + random.nextDouble() * 2;
            size = 10 + random.nextDouble() * 30;
            opacity = 0.1 + random.nextDouble() * 0.4;
            color = new Color(
                random.nextInt(100) + 156,
                random.nextInt(100) + 156,
                random.nextInt(100) + 156
            );
        }

        void move() {
            y -= speed;
            if (y + size < 0) {
                reset();
            }
        }

        void draw(Graphics2D g2d) {
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)opacity));
            g2d.setColor(color);
            g2d.fill(new Ellipse2D.Double(x, y, size, size));
            g2d.setComposite(originalComposite);
        }
    }

    public AnimatedBackground() {
        backgroundColor = new Color(20, 40, 60); // Dark blue background
        random = new Random();
        bubbles = new java.util.ArrayList<>();
        
        for (int i = 0; i < numBubbles; i++) {
            bubbles.add(new Bubble());
        }

        animationTimer = new javax.swing.Timer(50, e -> {
            for (Bubble bubble : bubbles) {
                bubble.move();
            }
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background gradient
        GradientPaint gradient = new GradientPaint(
            0, 0, backgroundColor,
            0, getHeight(), new Color(10, 20, 30)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw bubbles
        for (Bubble bubble : bubbles) {
            bubble.draw(g2d);
        }

        g2d.dispose();
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}
