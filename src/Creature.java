import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.Vector;

public class Creature extends Sprite {
	double rot;
	double hunger = 100;
	static Random rnd = new Random();
	boolean[] inputs = new boolean[Settings.inputcount];
	boolean[] outputs = new boolean[Settings.outputcount];
	boolean stopb = false;
	Network net;
	boolean hasfocus;
	boolean female, consenting, bred;
	int stamina;
	int age = 0;
	double prevlife = 0;

	public Creature(double x, double y, double rot1, int range) {
		super(x, y, 10, 40, range, new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
		rot = rot1;
		net = new Network();
		female = rnd.nextBoolean();
	}

	//region draw
	Shape view2(double theta, int dist) {
		return new Line2D.Double(getX(), -getY(), getX() + Math.cos(rot + theta) * dist,
				-getY() + Math.sin(rot + theta) * dist);
	}

	Shape health() {
		return new Rectangle2D.Double(getX() - 20 + 40 * life / maxlife, -getY() + 15, maxlife - 40 * life / maxlife,
				7);
	}

	Shape health2() {
		return new Rectangle2D.Double(getX() - 20, -getY() + 15, 40 * life / maxlife, 7);
	}

	Shape food() {
		return new Rectangle2D.Double(getX() - 20 + hunger * 2 / 5, -getY() + 22, 40 - hunger * 2 / 5, 7);
	}

	Shape food2() {
		return new Rectangle2D.Double(getX() - 20, -getY() + 22, hunger * 2 / 5, 7);
	}

	@Override
	void drawex(Graphics2D g2) {
		for (int i = 0; i < 5; i++) {//draw input lines
			g2.setColor(c);
			g2.setColor(new Color((inputs[9*i]?128:0)+(inputs[9*i+1]?64:0), (inputs[9*i+2]?128:0)+(inputs[9*i+3]?64:0), (inputs[9*i+4]?128:0)+(inputs[9*i+5]?64:0)));
			g2.draw(view2((i - 2) * Settings.viewspread, (int) (20 / (Math.abs(i - 2) / 2.0 + 1) + r)));
		}
		g2.setColor(new Color(200, 100, 100, 200));
		g2.fill(health());
		g2.fill(food());
		g2.setColor(new Color(100, 200, 100, 200));
		g2.fill(health2());
		g2.fill(food2());
	}
	//endregion

	@Override
	void update() {
		Vector<Sprite> set = EvolvePanel.tree.query(new Square(new Point(loc.x, -loc.y), 32+2*getR()), new Square(new Point(0, 0), 2*Settings.range));
		if (rnd.nextInt(100000-age) == 0)
			net.mutate();
		int l = 0;
		for(int i = 0; i < 5; i++){
			Sprite s = EvolvePanel.checksightall(this, (i-2) * Settings.viewspread);
			if(!(s instanceof NULLSP)){
				Color cr = s.c;
				inputs[l++] = Calc.binary(cr.getRed()  /64, 2)[0];
				inputs[l++] = Calc.binary(cr.getRed()  /64, 2)[1];
				inputs[l++] = Calc.binary(cr.getGreen()/64, 2)[0];
				inputs[l++] = Calc.binary(cr.getGreen()/64, 2)[1];
				inputs[l++] = Calc.binary(cr.getBlue() /64, 2)[0];
				inputs[l++] = Calc.binary(cr.getBlue() /64, 2)[1];
				inputs[l++] = Calc.binary((int) Math.floor(Arith.dist(s, this)/25), 2)[0];
				inputs[l++] = Calc.binary((int) Math.floor(Arith.dist(s, this)/25), 2)[1];
				inputs[l++] = s.data;
			}else {
				for(int j = 0; j < Settings.inPerView; j++){
					inputs[l++] = false;
				}
			}
		}
		inputs[45] = (life < prevlife);
		prevlife = life;
		net.run(inputs);
		if (!hasfocus)
			outputs = net.giveOutput();
		//region out
		//region basic control
		if (outputs[0]) {//forward
			move(Math.cos(rot) * 0.5, -Math.sin(rot) * 0.5);
			hunger -= 0.01;
		}
		if (outputs[1]) {//left
			move(Math.sin(rot) * 0.5, Math.cos(rot) * 0.5);
			hunger -= 0.01;
		}
		if (outputs[2]) {//back
			move(-Math.cos(rot) * 0.5, Math.sin(rot) * 0.5);
			hunger -= 0.01;
		}
		if (outputs[3]) {//right
			move(-Math.sin(rot) * 0.5, -Math.cos(rot) * 0.5);
			hunger -= 0.01;
		}
		if (outputs[4]) {//turn left
			rot -= 0.03;
		}
		if (outputs[5]) {//turn right
			rot += 0.03;
		}
		//endregion
		if (outputs[6]) {//eat & breed
			consenting = true;
			hunger -= 0.02;
			for (Food f : Arith.filter(Food.class, set)) {
				if (Calc.dist(f.getX() - getX(), f.getY() - getY()) < f.getR() + getR()) {
					if (hunger <= 97.5) {
						hunger += 2.5;
						f.incrementR(-1);
						if (f.getR() <= 0) {
							f.die();
						}
					}
					break;
				}
			}
			if (age >= 18 && stamina > 1000) {
				Creature breed = null;
				for (Creature cr : Arith.filter(Creature.class, set)) {
					if (cr != this && cr.female != female && cr.consenting && Arith.dist(cr, this) < r + cr.r && !bred
							&& cr.age >= 18) {
						bred = true;
						hunger -= 50;
						stamina = 0;
						double newcx, newcy;
						if (female) {
							newcx = getX();
							newcy = getY();
						} else {
							newcx = cr.getX();
							newcy = cr.getY();
						}
						breed = new Creature(newcx, newcy, rnd.nextInt(6), range);
						breed.c = Arith.mix(c, cr.c);
						breed.setnet(Arith.mix(net, cr.net));
						while ((new Random()).nextInt(100) == 0) {
							breed.net.mutate();
						}
						break;
					}
				}
				if (breed != null)
					EvolvePanel.addque.add(breed);
			}
		} else {
			consenting = false;
			bred = false;
			stamina++;
		}
		if (outputs[7]) {//shoot
			if (!stopb) {
				EvolvePanel.tree.add(new Bullet(getX() + Math.cos(rot) * 30, getY() - Math.sin(rot) * 30, rot, c,
						Settings.range));
				hunger -= 0.25;
				stopb = true;
			}
		} else
			stopb = false;
		data = outputs[8];
		//endregion
		//region hunger
		if (rnd.nextInt(50-(int) life/5) == 0 && hunger > 0) {
			hunger -= 0.5;
		}
		if (hunger < 10 && rnd.nextInt(100) == 0) {
			life--;
		}
		if (hunger > 90 && rnd.nextInt(50) == 0 && life < maxlife) {
			life++;
		}
		if (hunger < 0) {
			life += hunger / 10;
			hunger = 0;
		}
		//endregion
		for (Bullet b : Arith.filter(Bullet.class, set)) {
			if (Calc.dist(getX() - b.getX(), getY() - b.getY()) <= b.getR() + getR()) {
				life -= 15 - Calc.dist(getX() - b.getX(), getY() - b.getY());
				EvolvePanel.tree.remove(b);
				break;
			}
		}
		if (rnd.nextInt(1000) == 0)
			age++;
	}

	synchronized void die() {
		EvolvePanel.tree.add(Arith.range(new Food(getX(), getY(), 15, true, Settings.range)));
		EvolvePanel.tree.remove(this);
	}

	void runNet() {
		net.run();
	}

	void setnet(Network newnet) {
		net = newnet;
	}
}