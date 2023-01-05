import java.awt.*;
import java.awt.geom.Point2D;

public class Main {

    public static Dimension size = new Dimension(750, 750);

    public static Game.Level[] levels = new Game.Level[] {
            new Game.Level(Game.Tile.dev)
    };

    public static Game.Player player = new Game.Player(24, 24);
    public static Game.World world = new Game.World(levels, player);

    public static Gui.Frame frame = new Gui.Frame();
    public static Gui.Panel panel = new Gui.Panel(size);
    public static Gui.Listener listener = new Gui.Listener();

    public static void main(String[] args) {
        frame.add(panel);
        frame.addKeyListener(listener);
        frame.pack();

        Utilities.GameRunner game = new Utilities.GameRunner();
        game.start();
    }
}