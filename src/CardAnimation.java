import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CardAnimation {
    private Timer timer;
    private double progress;
    private Point start;
    private Point end;
    private JComponent component;
    private Consumer<Double> onProgress;
    private Runnable onComplete;
    private static final int ANIMATION_DURATION = 500; // milliseconds
    private static final int TIMER_INTERVAL = 16; // ~60 FPS

    public CardAnimation(JComponent component, Point start, Point end) {
        this.component = component;
        this.start = start;
        this.end = end;
        this.progress = 0;
    }

    public void setOnProgress(Consumer<Double> onProgress) {
        this.onProgress = onProgress;
    }

    public void setOnComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    public void start() {
        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(TIMER_INTERVAL, e -> {
            progress += (double) TIMER_INTERVAL / ANIMATION_DURATION;
            
            if (progress >= 1.0) {
                progress = 1.0;
                timer.stop();
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            // Ease in-out cubic
            double easedProgress = progress < 0.5 
                ? 4 * progress * progress * progress 
                : 1 - Math.pow(-2 * progress + 2, 3) / 2;

            if (onProgress != null) {
                onProgress.accept(easedProgress);
            }

            // Update component position
            Point currentPos = new Point(
                (int) (start.x + (end.x - start.x) * easedProgress),
                (int) (start.y + (end.y - start.y) * easedProgress)
            );
            component.setLocation(currentPos);
            component.repaint();
        });

        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }
}
