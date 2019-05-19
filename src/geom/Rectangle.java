package geom;
import java.awt.Graphics2D;
import calc.Arith;


public class Rectangle implements Shape{
    Point start, end;

    public Rectangle(Point start, double w, double h) {
        this(start, start.retmove(w, -h));
    }

    public Rectangle(Point start, Point end) {
        this.start = new Point(Arith.min(start.x, end.x), Arith.max(start.y, end.y));
        this.end = new Point(Arith.max(start.x, end.x), Arith.min(start.y, end.y));
    }

    public boolean intersects(Rectangle r) {
        return start.x < r.end.x && end.x > r.start.x && start.y > r.end.y && end.y < r.start.y;
    }

    public boolean intersects(Shape s) {
        return s.intersects(this);
    }

    double getWidth() {
        return end.x - start.x;
    }

    double getHeight() {
        return start.y - end.y;
    }

    Point centerPoint() {
        return new Point((start.x+end.x)/2, (start.y+end.y)/2);
    }

    void move(double x, double y){
        start.move(x, y);
        end.move(x, y);
    }

    Rectangle retmove(double x, double y){
        return new Rectangle(start.retmove(x, y),
        end.retmove(x, y));
    }

    public Rectangle corner(int x, int y){
        Rectangle corner = new Rectangle(new Point(start.getX(), start.getY()),
        centerPoint());
        corner.move(corner.getWidth()*x, -corner.getHeight()*y);
        return corner;
    }
    Point cornerPoint(double x, double y){
        return start.retmove(getWidth()*x, -getHeight()*y);
    }

    public void draw(Graphics2D g){
        g.drawRect((int) start.getX(), (int) -start.getY(), (int) getWidth(), (int) getHeight());
    }

    public boolean contains(Point p){
        return (p.x>start.x&&p.x<end.x&&p.y<start.y&&p.y>end.y);
    }
    void increment(double i){
        start.move(-i, -i);
        end.move(i, i);
    }
    Rectangle retincrement(double i){
        return new Rectangle(start.retmove(-i, -i), getWidth()+2*i, getHeight()+2*i);
    }
}