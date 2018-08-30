package sprites;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import calc.Settings;
import geom.Point;
import managers.QuadTree;

abstract public class Sprite {
	double r;
	public Point loc;
	double maxlife, life;
	public Point innerloc;
	public int range;
	Color c;
	public String name;
	boolean data = false;
	public QuadTree holder;

	public Sprite(double x, double y, double r1, int health, int range, Color c) {
		r = r1;
		innerloc = new Point((x + range) / (2.0 * range), (-y + range) / (2.0 * range));
		this.range = range;
		maxlife = life = health;
		loc = new Point(x, y);
		this.c = c;
		name = "";
	}
	
	public double getX() {
		return loc.getX();
	}

	public double getY() {
		return loc.getY();
	}

	public double getR() {
		return r;
	}

	public void move(double x1, double y1) {
		loc.move(x1, y1);
		innerloc.move(x1 / (2 * range), -y1 / (2 * range));
	}

	void incrementR(double add) {
		r += add;
	}

	public Ellipse2D view() {
		return new Ellipse2D.Double(loc.getX() - r, -loc.getY() - r, 2 * r, 2 * r);
	}

	public void run() {
		update();
		if (life <= 0 || loc.getX() < -Settings.range || loc.getX() > Settings.range || loc.getY() < -Settings.range
				|| loc.getY() > Settings.range || r <= 0)
			die();
	}

	abstract void update();

	synchronized void die() {
		holder.remove(this);
	}

	public void draw(Graphics2D g2) {
		drawex(g2);
		g2.setColor(c);
		g2.fill(view());
		g2.setColor(Color.BLACK);
		Font font = g2.getFont();
		Font f = new Font(font.getFontName(), font.getStyle(), font.getSize() / 4);
		g2.setFont(f);
		FontMetrics fm = g2.getFontMetrics(f);
		g2.drawString(name, (int) (loc.getX() - fm.stringWidth(name) / 2), (int) (-loc.getY() - fm.getHeight() / 2));
		g2.setFont(font);
	}

	abstract void drawex(Graphics2D g2);
}
