package sprites;
import java.awt.Color;
import java.awt.Graphics2D;

public class Food extends Sprite {
	final Boolean meat;

	public Food(double x, double y, float size1, Boolean meat, int range) {
		super(x, y, size1, meat ? 1000 : 10000, range, meat ? Color.PINK : Color.GREEN);
		this.meat = meat;
		data = meat;
	}

	@Override
	void update() {
		r-=0.005;
	}

	@Override
	void drawex(Graphics2D g2) {}
}
