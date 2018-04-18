
public class Point {
	double x, y;
	Point(double x1, double y1){
		x = x1;
		y = y1;
	}
	double getX() {
		return x;
	}
	double getY() {
		return y;
	}
	void move(double x1, double y1) {
		x += x1;
		y += y1;
	}
	Point retmove(double x1, double y1) {
		return new Point(x + x1, y + y1);
	}
}
