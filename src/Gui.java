import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public final class Gui {
    public static class Panel extends JPanel{
        private final BufferedImage buffer;

        public Panel(Dimension size) {
            setSize(size);
            setPreferredSize(size);
            setVisible(true);
            buffer = new BufferedImage(getWidth(), getHeight(), Image.SCALE_DEFAULT);
        }

        public void render(Drawable d) {
            d.drawSelf(buffer.getGraphics());
        }

        public void paint(Graphics g) {
            g.clearRect(0, 0, getWidth(), getHeight());
            Main.world.drawSelf(g);
            //g.drawImage(buffer, 0, 0, getWidth(), getHeight(), 0, 0, buffer.getWidth(), buffer.getHeight(), null);
        }
    }

    public static class Frame extends JFrame {
        public Frame() {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setResizable(false);
            setVisible(true);
        }
    }

    public static class Listener implements KeyListener {

        private final ArrayList<Integer> inputs = new ArrayList<>();
        private Integer input = null;

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (!inputs.contains(e.getKeyCode())) {
                inputs.add(e.getKeyCode());
            }

            input = e.getKeyCode();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            inputs.removeIf(input -> input.equals(e.getKeyCode()));

            if (inputs.size() == 0) {
                input = null;
            } else if (inputs.size() == 1) {
                input = inputs.get(0);
            }
        }

        public ArrayList<Integer> getInputs() {
            return inputs;
        }

        public Integer getCurrentInput() {
            return input;
        }
    }

    public interface Drawable {
        void drawSelf(Graphics g);
    }
}

