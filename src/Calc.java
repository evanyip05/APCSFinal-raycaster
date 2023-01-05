import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// all the math
public final class Calc {

    // 5/2 realized that these could just be method calls...
    // using math
    public static Function<Double, Double> degToRad = (deg) -> (deg*Math.PI)/180;
    public static Function<Double, Double> radToDeg = (rad) -> (rad*180)/Math.PI;

    // from face, x/y
    public static BiFunction<Double, Double, Double> yComp = (deg, radius) -> radius*Math.sin(degToRad.apply(deg));
    public static BiFunction<Double, Double, Double> xComp = (deg, radius) -> radius*Math.cos(degToRad.apply(deg));

    public static BiFunction<Point2D, Point2D, Double> degFromPoint = (center, point) -> (point.getY()-center.getY() >= 0) ?
                    radToDeg.apply(Math.atan2(point.getY()-center.getY(), point.getX()-center.getX())) :
                    360 + radToDeg.apply(Math.atan2(point.getY()-center.getY(), point.getX()-center.getX()));

    // never used
    public static double degFromPoint(Point2D.Double center, Point2D.Double point) {
        if (point.y-center.y >= 0) {
            return radToDeg.apply(Math.atan2(point.y-center.y, point.x-center.x));
        } else {
            return 360 + radToDeg.apply(Math.atan2(point.y-center.y, point.x-center.x));
        }
    }

    // get the closest point to line origin point line.getA()
    public static Point2D closestPointToLineOrigin(Line line, ArrayList<Point2D> points) {
        Point2D closest = line.getB();

        for (Point2D point : points) {
            if (    Math.abs(line.getA().getX()-point.getX()) <=
                    Math.abs(line.getA().getX()-closest.getX()) &&
                    Math.abs(line.getA().getY()-point.getY()) <=
                    Math.abs(line.getA().getY()-closest.getY())
               )

            {
                closest = point;
            }
        }

        return closest;
    }

    // check if a point is within the x and y bounds of a lines 2 points
    public static boolean pointInBoundingBox(Line line, Point2D point) {
        Point2D a = line.getA(), b = line.getB();
        double minY, maxY, minX, maxX;

        if (line.getA().getX() > line.getB().getX()) {
            maxX = a.getX();
            minX = b.getX();
        } else {
            minX = a.getX();
            maxX = b.getX();
        }

        if (line.getA().getY() > line.getB().getY()) {
            maxY = a.getY();
            minY = b.getY();
        } else {
            minY = a.getY();
            maxY = b.getY();
        }

        return point.getX() >= minX && point.getX() <= maxX && point.getY() >= minY && point.getY() <= maxY;
    }

    // line, defined by two points
    public static class Line implements Utilities.Drawable {

        private final Tuple<Point2D> line;
        public final boolean isVertical;

        public Line(Point2D a, Point2D b) {
            line = new Tuple<>(a, b);
            isVertical = (a.getX() == b.getX());
        }

        public double getSlope() {
            return (line.b.getY() - line.a.getY()) / (line.b.getX() - line.a.getX());
        }

        public double getYInt() {
            return line.a.getY() - (line.a.getX() * getSlope());
        }

        public Point2D getA() {
            return line.a;
        }

        public Point2D getB() {
            return line.b;
        }

        // get the intersection of two points, with a check for vertical lines
        public Point2D getIntersection(Line other) {
            double x = (getYInt() - other.getYInt()) / (other.getSlope() - getSlope());
            double y = (getSlope() * x) + getYInt();

            if (!isVertical && !other.isVertical) {
                return new Point2D.Double(x, y);
            } else {
                if (other.isVertical) {
                    return new Point2D.Double(other.getA().getX(), getValue(other.getA().getX()));
                } else {
                    return new Point2D.Double(line.a.getX(), other.getValue(line.a.getX()));
                }
            }
        }

        public double getLength() {
            Point2D a, b;

            a = (getA().getX() > getB().getX()) ? getB() : getA();
            b = (getA().getX() > getB().getX()) ? getA() : getB();

            return Math.sqrt(Math.pow(b.getX()-a.getX(),2) + Math.pow(b.getY()-a.getY(),2));
        }

        // get a value of a curve
        public double getValue(double x) {
            return (getSlope() * x) + getYInt();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Line) {
                return (line.a.equals(((Line) other).getA()) && line.b.equals(((Line) other).getB()));
            } else {
                return false;
            }
        }

        @Override
        public void drawSelf(Graphics g) {
            g.drawLine((int) line.a.getX(),(int) line.a.getY(),(int) line.b.getX(),(int) line.b.getY());
        }
    }

    public static class Tuple<E> {
        public final E a, b;

        public Tuple(E a, E b) {
            this.a = a;
            this.b = b;
        }

        public void affectAll(Consumer<E> action) {
            action.accept(a);
            action.accept(b);
        }

        public ArrayList<E> getAsList() {
            ArrayList<E> res = new ArrayList<>();
            res.add(a);
            res.add(b);

            return res;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Tuple) {
                if (((Tuple<?>) o).a.getClass() == a.getClass()) {
                    return ((((Tuple<?>) o).a == a) && (((Tuple<?>) o).b == b)) || ((((Tuple<?>) o).b == a) && (((Tuple<?>) o).a == b));
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return a + " " + b;
        }
    }
}
