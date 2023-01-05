import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public final class Game {

    public static class World implements Gui.Drawable {
        private final ArrayList<Level> levels = new ArrayList<>();
        private final Player player;

        public World(Level[] levels, Player player) {
            this.levels.addAll(Arrays.asList(levels));
            this.player = player;
            player.setCurrentWorld(this);
        }

        @Override
        public void drawSelf(Graphics g) {
            //mini map buffer
            BufferedImage miniMap = new BufferedImage(
                    levels.get(player.getCurrentLevel()).getWidth()*Tile.tileSize,
                    levels.get(player.getCurrentLevel()).getHeight()*Tile.tileSize,
                    BufferedImage.TYPE_INT_RGB
            );

            // render buffer
            BufferedImage render =  new BufferedImage(
                    levels.get(player.getCurrentLevel()).getWidth()*Tile.tileSize,
                    levels.get(player.getCurrentLevel()).getHeight()*Tile.tileSize,
                    BufferedImage.TRANSLUCENT
            );

            // get graphics for buffers
            Graphics miniMapGraphics = miniMap.getGraphics();
            Graphics renderGraphics = render.getGraphics();

            //renderGraphics.setColor(Color.BLUE);
            //renderGraphics.fillRect(0, 0, render.getWidth(), render.getHeight());

            // draw content to buffers
            levels.get(player.currentLevel).drawSelf(miniMapGraphics);
            player.multiRayCast(renderGraphics);
            player.singleRayCast(miniMapGraphics);



            // draw render, cast to screen from map size
            g.drawImage(render,  0, 0, Main.size.width, Main.size.height,
                    0, 0, render.getWidth(), render.getHeight(), null
            );

            // draw mini map over render, cast to 100x100 space in the top left
            g.drawImage(miniMap, 0, 0, 100, 100,
                    0, 0, miniMap.getWidth(), miniMap.getHeight(), null
            );


        }
    }

    public static class Player extends Utilities.PositionableObject {
        private int currentLevel = 0, deg, fov = 95, rays = 95;
        private World currentWorld;

        public Player(int x, int y) {
            super(x, y);
            setCurrentLevel(0);
        }

        public void setCurrentWorld(World currentWorld) {
            this.currentWorld = currentWorld;
        }

        public void setCurrentLevel(int currentLevel) {
            this.currentLevel = currentLevel;
        }

        public int getCurrentLevel() {
            return currentLevel;
        }

        // move in a direction
        public void moveDir(int magnitude, boolean backwards) {
            if (!backwards) {
                translate( (Calc.xComp.apply(deg + 0.0, magnitude + 0.0) + 0.0), (Calc.yComp.apply(deg + 0.0, magnitude + 0.0) + 0.0));
            } else {
                translate((Calc.xComp.apply(deg + 0.0, -magnitude + 0.0) + 0.0), (Calc.yComp.apply(deg + 0.0, -magnitude + 0.0) + 0.0));

            }
        }

        public void strafe(int magnitude, boolean left) {
            if (left) {
                translate((Calc.xComp.apply(deg - 90.0, -1 + 0.0) + 0.0), (Calc.yComp.apply(deg - 90.0, -1 + 0.0) + 0.0));
            } else {
                translate((Calc.xComp.apply(deg + 90.0, -1 + 0.0) + 0.0), (Calc.yComp.apply(deg + 90.0, -1 + 0.0) + 0.0));

            }
        }

        // move (called in runnable in utilities)
        public void move(int input) {
            int speed = 1;
            switch (input) {
                case 34:
                    strafe(1, true);
                    break; // left
                case 38:
                    moveDir(1, false);
                    break; // forward
                case 33:
                    strafe(1, false);
                    break; // right
                case 40:
                    moveDir(1, true);
                    break; // down
                case 37:
                    --deg;
                    break; // turn left
                case 39:
                    ++deg;
                    break; // turn right
                case 44:
                    if (fov < 360) {
                        ++fov;
                        ++rays;
                    }
                    break;
                case 46:
                    if (fov > 5) {
                        --fov;
                        --rays;
                    }
                    break;
                default:
                    System.out.println(input);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }

        // raycast for direction
        public void singleRayCast(Graphics g) {
            int viewDistance = 100;

            g.setColor(Color.WHITE);

            // line from player to point at view distance at degrees
            Calc.Line ray = new Calc.Line(
                    getAsPoint(),
                    new Point2D.Double(
                            Calc.xComp.apply(deg + 0.0, viewDistance + 0.0) + getX(),
                            Calc.yComp.apply(deg + 0.0, viewDistance + 0.0) + getY()
                    )
            );

            ray.drawSelf(g);
        }

        public void multiRayCast(Graphics g) {
            int viewDistance = 500, scanLines = 5;
            double rFactor =(double) .1, gFactor =(double) .1, bFactor = .1;

            // units between (rays) for fov angle
            double dRay = (double) Main.size.width/(rays*2);
            double ds = dRay /scanLines;
            // accumulated dist
            double dist = 0;
            int rayCount = 0;

            // iterate from half fov before angle to half fov from angle
            for (int i = deg-(fov/2); i < deg+(fov/2); i += fov/rays) {
                double scanDist = 0;
                // make a new ray from player to specific dest defined by current degree being drawn
                Calc.Line ray = new Calc.Line(
                        getAsPoint(),
                        new Point2D.Double(
                                Calc.xComp.apply(i + 0.0, viewDistance + 0.0) + getX(),
                                Calc.yComp.apply(i + 0.0, viewDistance + 0.0) + getY()
                        )
                );

                // get intersections with walls
                ArrayList<Point2D> intersects = currentWorld.levels.get(currentLevel).getWallInts(ray);

                if (intersects.size() > 0) {

                    // readjust the line so it goes to closest intersect
                    ray = new Calc.Line(
                            getAsPoint(),
                            new Point2D.Double(
                                    Calc.closestPointToLineOrigin(ray, intersects).getX(),
                                    Calc.closestPointToLineOrigin(ray, intersects).getY()
                            )
                    );
                }

                if (rayCount%4 ==0 ) {
                    rFactor =0* incrementHueFactor(rFactor, .01);
                    gFactor =0* incrementHueFactor(gFactor, .01);
                    bFactor =0* incrementHueFactor(bFactor, .01);
                }

                bFactor = 1;

                double multiplicand = viewDistance/ray.getLength();
                int cFactor = 25;
                int red = capHue(rFactor*cFactor*multiplicand );
                int green = capHue(gFactor*cFactor*multiplicand );
                int blue = capHue(bFactor*cFactor*multiplicand);


                // draw a scan line based on the length of the line from player to wall
                if (intersects.size() > 0) {
                    //g.setColor(new Color((int) (250-(ray.getLength()-100)*250)));
                    g.setColor(new Color(red, green,blue));
                    for (int s = 0; s < scanLines; s++) {
                        g.drawLine((int) (dist+scanDist), 64, (int) (dist+scanDist), (int) (64 - ((1500 / (ray.getLength())))));
                        g.drawLine((int) (dist+scanDist), 64, (int) (dist+scanDist), (int) (64 + ((1500 / (ray.getLength())))));
                        scanDist+=ds;
                    }
                } else {
                    g.setColor(Color.black);
                    g.drawLine((int) dist, 64, (int) dist, (64 - 5));
                    g.drawLine((int) dist, 64, (int) dist, (64 + 5));
                }



                // move to pos of next scan line
                dist += dRay;
                ++rayCount;
            }
        }

        private double incrementHueFactor(double hueFactor, double increment) {
            Random random = new Random();

            if(random.nextBoolean()) {
                if (hueFactor < .3) {
                    hueFactor += increment;
                }
            } else {
                if (hueFactor > -.3) {
                    hueFactor -= increment;
                }
            }

            return hueFactor;
        }

        private int capHue(double hue) {
            if (hue > 255) {
                return 255;
            } else if (hue < 0) {
                return 0;
            } else {
                return (int) hue;
            }
        }
    }

    public static class Level implements Gui.Drawable {
        private final Tile[][] map;
        public final ArrayList<Calc.Line> walls = new ArrayList<>();

        public Level(Tile[][] map) {
            this.map = map;
            generateWalls();
        }

        // make all the lines of the walls of a tile array
        private void generateWalls() {
            for (int row = 0; row < map.length; ++row) {
                for (int tile = 0; tile < map[0].length; ++tile) {
                    if (map[row][tile].solid) {
                        int x1 = (tile * Tile.tileSize);
                        int y1 = (row * Tile.tileSize);
                        int x2 = (x1+(Tile.tileSize));
                        int y2 = (y1+(Tile.tileSize));

                        walls.add(new Calc.Line(new Point2D.Double(x1, y1), new Point2D.Double(x2, y1)));
                        walls.add(new Calc.Line(new Point2D.Double(x1, y2), new Point2D.Double(x2, y2)));
                        walls.add(new Calc.Line(new Point2D.Double(x1, y1), new Point2D.Double(x1, y2)));
                        walls.add(new Calc.Line(new Point2D.Double(x2, y1), new Point2D.Double(x2, y2)));
                    }
                }
            }
        }

        // return any intersects of a ray with the walls,
        // make sure intersects are on segments of both walls and ray
        public ArrayList<Point2D> getWallInts(Calc.Line ray) {
            ArrayList<Point2D> res1 = new ArrayList<>();
            ArrayList<Point2D> res2 = new ArrayList<>();

            for (Calc.Line wall : walls) {
                Point2D intersect = wall.getIntersection(ray);

                res1.add(intersect);
            }

            for (Calc.Line wall : walls) {
                for (Point2D intersect : res1) {

                    if (Calc.pointInBoundingBox(wall, intersect) && Calc.pointInBoundingBox(ray, intersect)) {
                        res2.add(intersect);
                    }
                }
            }


            return res2;
        }

        public int getWidth() {
            return map[0].length;
        }

        public int getHeight() {
            return map.length;
        }

        // draw tiles and wall lines
        @Override
        public void drawSelf(Graphics g) {
            for (int row = 0; row < map.length; ++row) {
                for (int tile = 0; tile < map[0].length; ++tile) {
                    map[row][tile].drawSelf(g);
                    g.fillRect((tile)*Tile.tileSize, (row)*Tile.tileSize, Tile.tileSize, Tile.tileSize);
                }
            }

            for (Calc.Line line : walls) {
                g.setColor(Color.GREEN);
                line.drawSelf(g);
            }
        }
    }

    // user defined tile types, tile[] are also stored here
    public enum Tile implements Gui.Drawable {
        // 3 char names?
        FL1(0, "", false, true),
        FL2(1, "", false, true),
        WLL(4, "",  true, false);

        public final boolean solid, walkable;
        public final Image sprite;
        public final int type;

        public final static int tileSize = 16;

        Tile(int type, String fileDir, boolean solid, boolean walkable) {
            this.type = type;
            this.solid = solid;
            this.walkable = walkable;

            sprite = new ImageIcon(fileDir).getImage();
        }

        // prepare the color, no position info, unfortunately so can't do more
        @Override
        public void drawSelf(Graphics g) {
            switch (type) {
                case 2:
                case 0: g.setColor(Color.GRAY); break;
                case 3:
                case 1: g.setColor(Color.LIGHT_GRAY); break;
                case 4: g.setColor(Color.BLUE);
            }
        }

        public static Tile[][] dev = new Tile[][] {
                {WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL},

                {WLL,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   WLL},

                {WLL,   FL2,   FL1,   WLL,   FL1,   WLL,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   WLL},

                {WLL,   FL1,   FL2,   FL1,   FL2,   FL1,   WLL,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   WLL},

                {WLL,   FL2,   FL1,   WLL,   FL1,   WLL,   FL1,   FL2,   FL1,   WLL,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2},

                {WLL,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   WLL,   FL1,   FL2,   FL1,   FL2,   FL1},

                {WLL,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   WLL,   FL1,   FL2,   FL1,   FL2,   FL1,   WLL},

                {WLL,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   WLL},

                {WLL,   FL2,   FL1,   FL2,   FL1,   FL2,   WLL,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   WLL},

                {WLL,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   WLL,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   WLL},

                {WLL,   FL2,   FL1,   FL2,   WLL,   FL2,   WLL,   FL2,   WLL,   WLL,   WLL,   WLL,   FL1,   FL2,   FL1,   WLL},

                {WLL,   FL1,   FL2,   FL1,   WLL,   FL1,   FL2,   FL1,   WLL,   FL1,   FL2,   WLL,   FL2,   FL1,   FL2,   WLL},

                {WLL,   FL2,   FL1,   FL2,   FL1,   WLL,   FL1,   FL2,   WLL,   FL2,   FL1,   WLL,   FL1,   FL2,   FL1,   WLL},

                {WLL,   FL1,   FL2,   FL1,   FL2,   FL1,   WLL,   WLL,   FL2,   FL1,   FL2,   WLL,   FL2,   FL1,   FL2,   WLL},

                {WLL,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   FL2,   FL1,   WLL},

                {WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL,   WLL},
        };
    }
}
