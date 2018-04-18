import java.awt.Color;
import java.awt.Graphics2D;

public class Bullet extends Sprite {
	double rot;
	double momentum = 5;
	double rise, run;
	public Bullet(double x, double y, double rot1, Color c1, int range) {
		super(x, y, 5, 1000, range, c1);
		rot = rot1;
		run = Math.cos(-rot);
		rise = Math.sin(-rot);
	}

	@Override
	void update() {
		momentum -= 0.05;
		move(momentum * run, momentum * rise);
		if(momentum <= 0)die();
	}

	@Override
	void drawex(Graphics2D g2) {
	}
}
