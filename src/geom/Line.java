package geom;

import java.awt.Graphics2D;

public class Line implements Shape {
    Point a, b;

    public Line(Point a, Point b) {
        if (a.x < b.x) {
            this.a = a;
            this.b = b;
        } else if (a.x == b.x) {
            if (a.y > b.y) {
                this.a = a;
                this.b = b;
            } else {
                this.a = b;
                this.b = a;
            }
        } else {
            this.a = b;
            this.b = a;
        }
    }

    double slope() {
        return (a.y - b.y) / (a.x - b.x);
    }

    double height() {
        return a.y - slope() * a.x;
    }

    public boolean intersects(Rectangle r) {
        return intersects(new Line(r.cornerPoint(0, 0), r.cornerPoint(1, 0)))
                || intersects(new Line(r.cornerPoint(1, 0), r.cornerPoint(1, 1)))
                || intersects(new Line(r.cornerPoint(1, 1), r.cornerPoint(0, 1)))
                || intersects(new Line(r.cornerPoint(0, 1), r.cornerPoint(0, 0))) || r.contains(a) || r.contains(b);
    }

    boolean intersects(Line l) {
        double xintersection = intersection(l);
        double yintersection = equation(xintersection);
        return xintersection <= b.x && xintersection <= l.b.x && a.x <= xintersection && l.a.x <= xintersection
                && (((yintersection <= l.a.y ^ yintersection <= l.b.y)
                        && (yintersection >= l.a.y ^ yintersection >= l.b.y)) || l.a.y == l.b.y)
                && (((yintersection <= a.y ^ yintersection <= b.y) && (yintersection >= a.y ^ yintersection >= b.y))
                        || a.y == b.y);
    }

    double intersection(Line l) {
        if (l.slope() >= Double.POSITIVE_INFINITY)
            return l.a.x;
        else if (slope() >= Double.POSITIVE_INFINITY)
            return a.x;
        return (shift() - l.shift()) / (l.slope() - slope());
    }

    double equation(double x) {
        if (slope() >= Double.POSITIVE_INFINITY)
            return (a.y + b.y) / 2;
        return slope() * x + shift();
    }

    double shift() {
        return a.y - a.x * slope();
    }

    void draw(Graphics2D g) {
        g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
    }
}