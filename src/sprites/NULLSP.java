package sprites;
import java.awt.Color;
import java.awt.Graphics2D;

public class NULLSP extends Sprite {
	public NULLSP() {
		super(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0, 0, Color.BLACK);
	}

	@Override
	void update() {
	}

	@Override
	void drawex(Graphics2D g2) {
	}
}
