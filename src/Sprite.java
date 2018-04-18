import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

abstract public class Sprite {
	double r;
	Point loc;
	double maxlife, life;
	Point innerloc;
	int range;
	Color c;
	String name;
	boolean data = false;

	public Sprite(double x, double y, double r1, int health, int range, Color c) {
		r = r1;
		innerloc = new Point((x + range) / (2.0 * range), (-y + range) / (2.0 * range));
		this.range = range;
		maxlife = life = health;
		loc = new Point(x, y);
		this.c = c;
		name = "NULL";
	}
	
	double getX() {
		return loc.getX();
	}

	double getY() {
		return loc.getY();
	}

	double getR() {
		return r;
	}

	void move(double x1, double y1) {
		loc.move(x1, y1);
		innerloc.move(x1 / (2 * range), -y1 / (2 * range));
	}

	void incrementR(double add) {
		r += add;
	}

	Ellipse2D view() {
		return new Ellipse2D.Double(loc.x - r, -loc.y - r, 2 * r, 2 * r);
	}

	void run() {
		update();
		if (life <= 0 || loc.x < -Settings.range || loc.x > Settings.range || loc.y < -Settings.range
				|| loc.y > Settings.range)
			die();
	}

	abstract void update();

	synchronized void die() {
		EvolvePanel.tree.remove(this);
	}

	void draw(Graphics2D g2) {
		drawex(g2);
		g2.setColor(c);
		g2.fill(view());
		// g2.setColor(Color.BLACK);
		// Font font = g2.getFont();
		// Font f = new Font(font.getFontName(), font.getStyle(), font.getSize() / 4);
		// g2.setFont(f);
		// FontMetrics fm = g2.getFontMetrics(f);
		// g2.drawString(name, (int) (loc.x - fm.stringWidth(name) / 2), (int) (-loc.y - fm.getHeight() / 2));
		// g2.setFont(font);
	}

	abstract void drawex(Graphics2D g2);
}
