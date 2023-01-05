import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

// math used to be here but moved it elsewhere
public final class Utilities {

    // self-explanatory
    public static abstract class PositionableObject {
        private double x, y;

        public PositionableObject(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void translate(double dx, double dy) {
            x+=dx;
            y+=dy;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public Point2D getAsPoint() {
            return new Point2D.Double(x, y);
        }

        @Override
        public String toString() {
            return "("+ x + " " + y + ")";
        }
    }

    public static class GameRunner extends ExtendableThread {

        @Override
        public void execute() throws InterruptedException {
            try {
                for (Integer input : Main.listener.getInputs()) {
                    Main.player.move(input);
                }
            } catch (ConcurrentModificationException | NoSuchElementException ignore){}

            Main.panel.repaint();

            Thread.sleep(1000/60);
        }

        @Override
        public boolean condition() {
            return true;
        }
    }

    // reusable thread I wrote, uses synchronized blocks and wait / notify to manage its lock
    // execute and condition need to be implemented in any subclasses
    public abstract static class ExtendableThread extends Thread {
        @Override
        public final void run() {
            while (condition()) {
                synchronized (this) {
                    try {

                        execute();

                        if (waitCondition()) {
                            wait();
                        }

                    }

                    catch (InterruptedException ignore) {}
                }
            }
        }

        public final void restart() {
            if (getState().equals(State.NEW)) {start();}

            synchronized (this) {
                executeOnRestart();
                notify();
            }
        }

        public void executeOnRestart() {}

        public boolean waitCondition() {return false;}

        public abstract void execute() throws InterruptedException;

        public abstract boolean condition();
    }

    public interface Drawable {
        void drawSelf(Graphics g);
    }
}
