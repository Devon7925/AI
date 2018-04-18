import java.awt.Graphics2D;

class Rectangle {
    Point start, end;

    public Rectangle(Point start, double w, double h) {
        this(start, start.retmove(w, h));
    }

    public Rectangle(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public boolean intersects(Rectangle r) {
        return start.x < r.end.x && end.x > r.start.x && start.y < r.end.y && end.y > r.start.y;
    }

    double getWidth() {
        return end.x - start.x;
    }

    double getHeight() {
        return end.y - start.y;
    }

    Point centerPoint() {
        return new Point(start.x+getWidth()/2, start.y+getHeight()/2);
    }

    void move(double x, double y){
        start.move(x, y);
        end.move(x, y);
    }

    Rectangle retmove(double x, double y){
        return new Rectangle(start.retmove(x, y),
        end.retmove(x, y));
    }

    Rectangle corner(int x, int y){
        Rectangle corner = new Rectangle(new Point(start.getX(), start.getY()),
        centerPoint());
        corner.move(corner.getWidth()*x, corner.getHeight()*y);
        return corner;
    }

    void draw(Graphics2D g){
        g.drawRect((int) start.getX(), (int) start.getY(), (int) getWidth(), (int) getHeight());
    }

    boolean contains(Point p){
        return (p.x>start.x&&p.x<end.x&&-p.y>start.y&&-p.y<end.y);
    }
}