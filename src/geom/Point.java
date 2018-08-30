package geom;

public class Point {
	double x, y;
	public Point(double x1, double y1){
		x = x1;
		y = y1;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public void move(double x1, double y1) {
		x += x1;
		y += y1;
	}
	Point retmove(double x1, double y1) {
		return new Point(x + x1, y + y1);
	}
}
