package calc;
import geom.Calc;
import geom.Line;
import geom.Point;
import geom.Square;
import managers.QuadTree;
import sprites.Creature;
import sprites.NULLSP;
import sprites.Sprite;

public class ViewHelper {
	public static Sprite checksightall(Creature cr, double d, QuadTree tree) {
		Sprite s = new NULLSP();
		Line l = new Line(new Point(cr.loc.getX(), cr.loc.getY()), new Point(cr.loc.getX() + Math.cos(cr.rot-d) * 150, cr.loc.getY() - Math.sin(cr.rot-d) * 150));
		for (Sprite sp : tree.query(l, new Square(new Point(0, 0), 2*Settings.range))) {
			if (!Arith.safecloser(cr, s, sp) && sp != cr && checksight(cr, d, sp)) {
				s = sp;
			}
		}
		return s;
	}

	static boolean checksight(Creature cr, double d, Sprite s) {
		d += Math.PI;
		double slope = Math.tan(-cr.rot - d);
		double a = Math.pow(slope, 2) + 1;
		double b = 2 * (slope * (cr.getY() - slope * cr.getX() - s.getY()) - s.getX());
		double c = Math.pow(cr.getY() - slope * cr.getX() - s.getY(), 2) + Math.pow(s.getX(), 2)
				- Math.pow(s.getR(), 2);
		double n = Math.abs((cr.rot + d + Math.PI / 2) % (2 * Math.PI)) - Math.PI;
		return (b * b - 4 * a * c >= 0 && Calc.dist((s.getX() - cr.getX()), (s.getY() - cr.getY())) <= 1000
				&& (n * (Calc.quad1(a, b, c) - cr.getX()) > 0 || n * (Calc.quad2(a, b, c) - cr.getX()) > 0));
	}

	static double checksightint(Creature cr, double d, Sprite s) {
		d += Math.PI;
		double slope = Math.tan(-cr.rot + d);
		double a = Math.pow(slope, 2) + 1;
		double b = 2 * (slope * (cr.getY() - slope * cr.getX() - s.getY()) - s.getX());
		double c = Math.pow(cr.getY() - slope * cr.getX() - s.getY(), 2) + Math.pow(s.getX(), 2)
				- Math.pow(s.getR() / 2, 2);
		return Calc.quad1(a, b, c);
	}
}