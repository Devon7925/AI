package managers;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import geom.Point;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import sprites.Creature;

public class ControlManager implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, Runnable {
	static public double zoom = 1, vx = 0, vy = 0, vx2 = 0, vy2 = 0;
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
			keyRIGHT = false,
			hasjoy = false;
	Controller joy;
	Point click;
	Creature focus;
    public AffineTransform tx = new AffineTransform();
    int time = 0;
    EvolvePanel root;
    Thread control;

    public ControlManager(EvolvePanel root){
        this.root = root;
		if (control == null) {
			control = new Thread(this, "That Main Thing");
			control.start();
		}
    }

	public void run(){
        initcontrol();
		addViewLoc(root.getWidth() / 2, root.getHeight() / 2);
        while(true){
            time++;
            if (hasjoy) {//run controller buttons
                joy.poll();
                if (joy.getComponents()[5].getPollData() == 1) {
                    if (focus != null) {
                        focus.hasfocus = false;
                    }
                    for (Creature c : root.tree.get(Creature.class)) {
                        if (tx.createTransformedShape(c.view()).contains(new Point2D.Double(1500, 1000))) {
                            focus = c;
                            focus.hasfocus = true;
                            break;
                        }
                    }
                }
                if (joy.getComponents()[6].getPollData() == 1 && focus != null) {
                    focus.hasfocus = false;
                    root.info.draweditor = false;
                    focus = null;
                }
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
            if (time % 10 == 0) {
                if (hasjoy) {
                    if (joy.getComponents()[7].getPollData() == 1) {
                        root.info.draweditor = !root.info.draweditor;
                    }
                    if (Math.abs(joy.getComponents()[1].getPollData()) > 0.05 && Math.abs(joy.getComponents()[0].getPollData()) > 0.05)
                        addViewLoc(-20 * joy.getComponents()[1].getPollData() / zoom, -20 * joy.getComponents()[0].getPollData() / zoom);
                    zoom *= Math.pow(2, -joy.getComponents()[4].getPollData() / 5);
					tx = new AffineTransform();
					tx.translate(root.getWidth() / 2, root.getHeight() / 2);
					tx.translate(vx, vy);
					tx.scale(zoom, zoom);
                }
                if (focus != null) {//move focused creature
                    focus.outputs[0] = keyW || (hasjoy && -joy.getComponents()[2].getPollData() > 0.6);
                    focus.outputs[1] = keyA || (hasjoy && -joy.getComponents()[3].getPollData() > 0.6);
                    focus.outputs[2] = keyS || (hasjoy && joy.getComponents()[2].getPollData() > 0.6);
                    focus.outputs[3] = keyD || (hasjoy && joy.getComponents()[3].getPollData() > 0.6);
                    focus.outputs[4] = keySHIFT || (hasjoy && joy.getComponents()[9].getPollData() > 0.9);
                    focus.outputs[5] = keySPACE || (hasjoy && joy.getComponents()[10].getPollData() > 0.9);
                    focus.outputs[6] = keyQ || (hasjoy && joy.getComponents()[12].getPollData() > 0.9);
                    focus.outputs[7] = keyE || (hasjoy && joy.getComponents()[11].getPollData() > 0.9);
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

	//region listeners
	@Override
	public void keyPressed(KeyEvent arg0) {
		int key = arg0.getKeyCode();
		if (key == KeyEvent.VK_ESCAPE) {
			if (focus != null) {
				if(root.info.draweditor){
					root.info.draweditor = false;
				}else{
					focus.hasfocus = false;
					focus = null;
				}
			} else{
				System.exit(0);
			}
		}
		if(key == KeyEvent.VK_ENTER){
			root.terminal.execute();
		}else if(arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE){
			root.terminal.remchar();
		}else if(arg0.getKeyChar() != '\uFFFF')root.terminal.type(arg0.getKeyChar());
		if(key == KeyEvent.VK_SLASH){
			root.terminal.activate();
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
		if(key == KeyEvent.VK_K)
			root.tree.KILL();
		if(key == KeyEvent.VK_P)
			root.paused = !root.paused;
		

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (root.info.c != null && !(root.info.scale().contains(e.getPoint()) || root.info.scaleEdit().contains(e.getPoint()))
				|| root.info.c == null) {
			if (focus != null) {
				focus.hasfocus = false;
				focus = null;
			}
			Creature[] checkC = root.tree.get(Creature.class).toArray(new Creature[0]);
			for (Creature c : checkC) {
				if (tx.createTransformedShape(c.view()).contains(e.getPoint())) {
					focus = c;
					focus.hasfocus = true;
					break;
				}
			}
			if (focus == null) {
				root.info.update(null, root.getSize());
				root.info.draweditor = false;
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
		if (root.info.c != null && !(root.info.scale().contains(e.getPoint()) || root.info.scaleEdit().contains(e.getPoint()))
				|| root.info.c == null) {
			vx2 = e.getX();
			vy2 = e.getY();
		} else {
			root.info.click(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (root.info.c != null && !(root.info.scale().contains(e.getPoint()) || root.info.scaleEdit().contains(e.getPoint()))
				|| root.info.c == null) {
					addViewLoc(e.getX()-vx2, e.getY()-vy2);
					vx2 = e.getX();
					vy2 = e.getY();
		} else {
			root.info.release(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (root.info.c != null && !(root.info.scale().contains(e.getPoint()) || root.info.scaleEdit().contains(e.getPoint()))
				|| root.info.c == null) {
					addViewLoc((e.getX()-vx2), (e.getY()-vy2));
					vx2 = e.getX();
					vy2 = e.getY();
		} else {
			root.info.drag(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		zoom(Math.pow(2, -e.getWheelRotation()));
		setViewLoc(-(vx)*Math.pow(2, -e.getWheelRotation()), (vy)*Math.pow(2, -e.getWheelRotation()));
	}

	//endregion
	public void setViewLoc(double newvx, double newvy) {
		vx = -newvx;
		vy = newvy;
		tx = new AffineTransform();
		tx.translate(root.getWidth() / 2, root.getHeight() / 2);
		tx.translate(vx, vy);
		tx.scale(zoom, zoom);
	}

	public void addViewLoc(double addvx, double addvy) {
		vx += addvx;
		vy += addvy;
		tx = new AffineTransform();
		tx.translate(root.getWidth() / 2, root.getHeight() / 2);
		tx.translate(vx, vy);
		tx.scale(zoom, zoom);
    }
    
	public void zoom(double factor) {
		zoom *= factor;
		tx = new AffineTransform();
		tx.translate(root.getWidth() / 2, root.getHeight() / 2);
		tx.translate(vx, vy);
		tx.scale(zoom, zoom);
	}
	
	public void setzoom(double factor) {
		zoom = factor;
		tx = new AffineTransform();
		tx.translate(root.getWidth() / 2, root.getHeight() / 2);
		tx.translate(vx, vy);
		tx.scale(zoom, zoom);
	}
	
	void initcontrol(){
		Controller[] control = ControllerEnvironment.getDefaultEnvironment().getControllers();
		joy = null;
		Component[] components = null;
		hasjoy = false;
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
	}
}