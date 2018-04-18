
//region imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

//endregion
public class Main extends JFrame {
	private static final long serialVersionUID = 1L;
	EvolvePanel panel = new EvolvePanel();

	public Main() {
		super("AI");
		setSize(2900, 1900);
		setLocation(20, 20);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		JPanel pane = new JPanel();
		pane.setLayout(new GridLayout(1, 1, 15, 15));
		pane.add(panel);
		setContentPane(pane);
		setAlwaysOnTop(true);
		addKeyListener(panel);
	}

	public static void main(String[] arguments) {
		Main clock = new Main();
		clock.setVisible(true);
	}
}

class EvolvePanel extends JPanel
		implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, Runnable {
	private static final long serialVersionUID = 1L;
	//region gen vars
	static ArrayList<Sprite> addque = new ArrayList<Sprite>();
	static double zoom = 1, vx = 0, vy = 0, vx2 = 0, vy2 = 0;
	boolean keyW = false, 
			keyA = false, 
			keyS = false, 
			keyD = false, 
			keyE = false, 
			keyQ = false, 
			keySHIFT = false, 
			keySPACE = false, 
			keyUP = false, 
			keyLEFT = false, 
			keyDOWN = false, 
			keyRIGHT = false;
	static Creature focus;
	Random rnd = new Random();
	double tempmousex, tempmousey;
	Point click;
	static Thread runner;
	AffineTransform tx = new AffineTransform();
	InfoPanel info;
	int time = 0;
	static QuadTree tree = new QuadTree();
	//endregion
	EvolvePanel() {
		//region add listeners
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		//endregion
		setBackground(Color.WHITE);

		Rectangle2D r = new Rectangle2D.Double(5.0 / 7, 1.0 / 20, 3.0 / 11, 9.0 / 10);
		info = new InfoPanel(r);
		if (runner == null) {
			runner = new Thread(this, "That Main Thing");
			runner.start();
		}
	}

	public void run() {
		Controller[] control = ControllerEnvironment.getDefaultEnvironment().getControllers();
		Controller joy = null;
		Component[] components = null;
		boolean hasjoy = false;
		for (int i = 0; i < control.length; i++) {//print components
			System.out.println(control[i].getName());
			if (control[i].getType().toString().equals("Gamepad")) {
				joy = control[i];
				hasjoy = true;
				System.out.println(i + ": " + control[i].toString());
				System.out.println("Type: " + control[i].getType().toString());
				components = joy.getComponents();
				System.out.println("Component Count: " + components.length);
				for (int j = 0; j < components.length; j++) {
					/* Get the components name */
					System.out.println("    Component " + j + ": " + components[j].getName());
					System.out.println("        Identifier: " + components[j].getIdentifier().getName());
					control[i].poll();
					System.out.println("        Value: " + components[j].getPollData());
				}
				break;
			}
		}
		addViewLoc(getWidth() / 2, getHeight() / 2);
		Thread t = new Thread();
		while (true) {
			try{
				t.join();
			}catch(Exception e){
				e.printStackTrace();
			}
			if (hasjoy) {//run controller buttons
				joy.poll();
				if (components[5].getPollData() == 1) {
					if (focus != null) {
						focus.hasfocus = false;
					}
					for (Creature c : tree.get(Creature.class)) {
						if (tx.createTransformedShape(c.view()).contains(new Point2D.Double(1500, 1000))) {
							focus = c;
							focus.hasfocus = true;
							break;
						}
					}
				}
				if (components[6].getPollData() == 1 && focus != null) {
					focus.hasfocus = false;
					info.draweditor = false;
					focus = null;
				}
			}
			tree.addAll(addque);
			addque.clear();
			time++;
			if (time % 10 == 0) {
				if (hasjoy) {
					if (components[7].getPollData() == 1) {
						info.draweditor = !info.draweditor;
					}
					if (Math.abs(components[1].getPollData()) > 0.05 && Math.abs(components[0].getPollData()) > 0.05)
						addViewLoc(-20 * components[1].getPollData() / zoom, -20 * components[0].getPollData() / zoom);
					zoom *= Math.pow(2, -components[4].getPollData() / 5);
					tx = new AffineTransform();
					tx.translate(getWidth() / 2, getHeight() / 2);
					tx.scale(zoom, zoom);
					tx.translate(-getWidth() / 2, -getHeight() / 2);
					tx.translate(vx, vy);
				}
				if (focus != null) {//move focused creature
					focus.outputs[0] = keyW || (hasjoy && -components[2].getPollData() > 0.6);
					focus.outputs[1] = keyA || (hasjoy && -components[3].getPollData() > 0.6);
					focus.outputs[2] = keyS || (hasjoy && components[2].getPollData() > 0.6);
					focus.outputs[3] = keyD || (hasjoy && components[3].getPollData() > 0.6);
					focus.outputs[4] = keySHIFT || (hasjoy && components[9].getPollData() > 0.9);
					focus.outputs[5] = keySPACE || (hasjoy && components[10].getPollData() > 0.9);
					focus.outputs[6] = keyQ || (hasjoy && components[12].getPollData() > 0.9);
					focus.outputs[7] = keyE || (hasjoy && components[11].getPollData() > 0.9);
				}
				//region move view
				if (keyUP) {
					addViewLoc(0, 4);
				}
				if (keyLEFT) {
					addViewLoc(4, 0);
				}
				if (keyDOWN) {
					addViewLoc(0, -4);
				}
				if (keyRIGHT) {
					addViewLoc(-4, 0);
				}
				//endregion
				if(time % 60 == 0){
					repaint();
					//region reorg & create
					if (tree.count(Creature.class) < Settings.creaturecap) {//create new creature
						Creature cr = new Creature(rnd.nextInt(2 * Settings.range) - Settings.range, rnd.nextInt(2 * Settings.range) - Settings.range,
						rnd.nextInt(360), Settings.range);
						cr.net.change(50);
						tree.add(cr);
					}
					if(tree.count(Food.class) < Settings.foodcap) {//create new food
						tree.add(new Food(rnd.nextInt(2*Settings.range)-Settings.range, rnd.nextInt(2*Settings.range)-Settings.range, rnd.nextInt(14)+2, false, Settings.range));
					}
					tree.reorg();
					//endregion
				}
				for(int i = 0; i < Settings.netPerCycle; i++){
					for (Creature cr : tree.get(Creature.class)) {//run networks
						cr.runNet();
					}
				}
				tree.runSP();
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void paintComponent(Graphics comp) {
		Graphics2D comp2D = (Graphics2D) comp;
		Font type = new Font("Serif", Font.BOLD, 24);
		comp2D.setFont(type);
		comp2D.setColor(getBackground());
		comp2D.fillRect(0, 0, getSize().width, getSize().height);
		AffineTransform saveXform = comp2D.getTransform();
		comp2D.transform(tx);
		try {
			comp2D.drawImage(ImageIO.read(new File("C:/Users/Public/eclipse-workspace/AI/dean-kamen.jpg")), -Settings.range, -Settings.range, 2*Settings.range, 2*Settings.range, null);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Point topleft = null, bottomright = null;
		try{
			topleft = new Point(tx.createInverse().transform(new Point2D.Double(0, 0), null).getX(), tx.createInverse().transform(new Point2D.Double(0, 0), null).getY());
			bottomright = new Point(tx.createInverse().transform(new Point2D.Double(getWidth(), getHeight()), null).getX(), tx.createInverse().transform(new Point2D.Double(getWidth(), getHeight()), null).getY());
		}catch(Exception e){
			e.printStackTrace();
		}
		Rectangle randrect = new Rectangle(topleft, bottomright);
		for(Sprite s : tree.query(randrect, new Rectangle(new Point(-Settings.range, -Settings.range), 2*Settings.range, 2*Settings.range))){
			s.draw(comp2D);
		}
		comp2D.setTransform(saveXform);
		if (focus != null) {
			info.update(focus, getSize());
			info.draw(comp2D);
		}
	}

	//region creature sight
	public static synchronized Sprite checksightall(Creature cr, double d) {
		double dist = 1000;
		Sprite s = new NULLSP();
		for (Creature c : tree.get(Creature.class)) {
			if (c != cr && checksight(cr, d, c) && Calc.dist(c.getX() - cr.getX(), c.getY() - cr.getY()) <= dist) {
				dist = Math.sqrt((c.getX() - cr.getX()) * (c.getX() - cr.getX())
						+ (c.getY() - cr.getY()) * (c.getY() - cr.getY()));
				if (!closer(cr, s, c))
					s = c;
			}
		}
		for (Food f : tree.get(Food.class)) {
			if (checksight(cr, d, f) && Calc.dist((f.getX() - cr.getX()), (f.getY() - cr.getY())) <= dist) {
				dist = Calc.dist(f.getX() - cr.getX(), f.getY() - cr.getY());
				if (!closer(cr, s, f))
					s = f;
			}
		}
		for (Bullet b : tree.get(Bullet.class)) {
			if (checksight(cr, d, b) && Calc.dist((b.getX() - cr.getX()), (b.getY() - cr.getY())) <= dist) {
				if (!closer(cr, s, b))
					s = b;
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

	//endregion
	//region listeners
	@Override
	public void keyPressed(KeyEvent arg0) {
		int key = arg0.getKeyCode();
		if (key == KeyEvent.VK_ESCAPE) {
			if (focus != null) {
				if(info.draweditor){
					info.draweditor = false;
				}else{
					focus.hasfocus = false;
					focus = null;
				}
			} else{
				System.exit(0);
			}
		}
		if (key == KeyEvent.VK_W)
			keyW = true;
		if (key == KeyEvent.VK_A)
			keyA = true;
		if (key == KeyEvent.VK_S)
			keyS = true;
		if (key == KeyEvent.VK_D)
			keyD = true;
		if (key == KeyEvent.VK_E)
			keyE = true;
		if (key == KeyEvent.VK_Q)
			keyQ = true;
		if (key == KeyEvent.VK_SHIFT)
			keySHIFT = true;
		if (key == KeyEvent.VK_SPACE)
			keySPACE = true;
		if (key == KeyEvent.VK_UP)
			keyUP = true;
		if (key == KeyEvent.VK_LEFT)
			keyLEFT = true;
		if (key == KeyEvent.VK_DOWN)
			keyDOWN = true;
		if (key == KeyEvent.VK_RIGHT)
			keyRIGHT = true;
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		int key = arg0.getKeyCode();
		if (key == KeyEvent.VK_W)
			keyW = false;
		if (key == KeyEvent.VK_A)
			keyA = false;
		if (key == KeyEvent.VK_S)
			keyS = false;
		if (key == KeyEvent.VK_D)
			keyD = false;
		if (key == KeyEvent.VK_E)
			keyE = false;
		if (key == KeyEvent.VK_Q)
			keyQ = false;
		if (key == KeyEvent.VK_SHIFT)
			keySHIFT = false;
		if (key == KeyEvent.VK_SPACE)
			keySPACE = false;
		if (key == KeyEvent.VK_UP)
			keyUP = false;
		if (key == KeyEvent.VK_LEFT)
			keyLEFT = false;
		if (key == KeyEvent.VK_DOWN)
			keyDOWN = false;
		if (key == KeyEvent.VK_RIGHT)
			keyRIGHT = false;
		if(key == KeyEvent.VK_K){
			tree.KILL();
		}

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (info.c != null && !(info.scale().contains(e.getPoint()) || info.scaleEdit().contains(e.getPoint()))
				|| info.c == null) {
			if (focus != null) {
				focus.hasfocus = false;
				focus = null;
			}
			Creature[] checkC = tree.get(Creature.class).toArray(new Creature[0]);
			for (Creature c : checkC) {
				if (tx.createTransformedShape(c.view()).contains(e.getPoint())) {
					focus = c;
					focus.hasfocus = true;
					break;
				}
			}
			if (focus == null) {
				info.update(null, getSize());
				info.draweditor = false;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (info.c != null && !(info.scale().contains(e.getPoint()) || info.scaleEdit().contains(e.getPoint()))
				|| info.c == null) {
			vx2 = vx;
			vy2 = vy;
			tempmousex = e.getX();
			tempmousey = e.getY();
		} else {
			info.click(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (info.c != null && !(info.scale().contains(e.getPoint()) || info.scaleEdit().contains(e.getPoint()))
				|| info.c == null) {
			vx = vx2 + (tempmousex - e.getX()) / zoom;
			vy = vy2 - (tempmousey - e.getY()) / zoom;
		} else {
			info.release(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (info.c != null && !(info.scale().contains(e.getPoint()) || info.scaleEdit().contains(e.getPoint()))
				|| info.c == null) {
			setViewLoc(vx2 - (tempmousex - e.getX()) / zoom, vy2 - (tempmousey - e.getY()) / zoom);
		} else {
			info.drag(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		zoom *= Math.pow(2, -e.getWheelRotation());
		try {
			Point2D p = tx.createInverse().transform(e.getPoint(), null);
			tx = new AffineTransform();
			tx.translate(getWidth() / 2, getHeight() / 2);
			tx.scale(zoom, zoom);
			tx.translate(-getWidth() / 2, -getHeight() / 2);
			Point2D p1 = tx.createInverse().transform(e.getPoint(), null);
			vx2 -= p.getX() - p1.getX();
			vy2 -= p.getY() - p1.getY();
			setViewLoc(vx2, vy2);
		} catch (NoninvertibleTransformException e0) {
			e0.printStackTrace();
		}
	}

	//endregion
	//region view control
	void setViewLoc(double newvx, double newvy) {
		tx.translate(-vx, -vy);
		vx = newvx;
		vy = newvy;
		tx.translate(vx, vy);
	}

	void addViewLoc(double addvx, double addvy) {
		vx += addvx;
		vy += addvy;
		tx.translate(addvx, addvy);
	}

	//endregion
	static boolean closer(Sprite ref, Sprite sr, Sprite s) {
		int type = Arith.TypeToID(sr.getClass());
		switch (type) {
		case 0:
			return false;
		case 1:
			return Arith.closer(ref, sr, s);
		case 2:
			return Arith.closer(ref, sr, s);
		case 3:
			return Arith.closer(ref, sr, s);
		}
		return false;
	}
}