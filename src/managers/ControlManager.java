package managers;

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
import java.util.HashMap;

import geom.Point;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import sprites.Creature;

public class ControlManager implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, Runnable {
    public double zoom = 1, vx = 0, vy = 0, vx2 = 0, vy2 = 0;
    HashMap<Integer, Boolean> keys = new HashMap<>();
    boolean hasjoy = false;
    Controller joy;
    Point click;
    Creature focus;
    public AffineTransform tx = new AffineTransform();
    int time = 0;
    EvolvePanel root;
    Thread control;

    public ControlManager(EvolvePanel root) {
        this.root = root;
        if (control == null) {
            control = new Thread(this, "That Main Thing");
            control.start();
        }
    }

    public Point2D project(Point2D screenCords) {
        try {
            return tx.createInverse().transform(screenCords, null);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            return new Point2D.Double();
        }
    }

    public void run() {
        initcontrol();
        addViewLoc(root.getWidth() / 2, root.getHeight() / 2);
        while (true) {
            time++;
            if (hasjoy) {// run controller buttons
                joy.poll();
                if (joy.getComponents()[5].getPollData() == 1) {
                    if (focus != null) {
                        focus.hasfocus = false;
                    }
                    for (Creature c : root.safeclone.get(Creature.class)) {
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
            // region move view
            if (keys.getOrDefault(KeyEvent.VK_UP, false)) {
                addViewLoc(0, 4);
            }
            if (keys.getOrDefault(KeyEvent.VK_LEFT, false)) {
                addViewLoc(4, 0);
            }
            if (keys.getOrDefault(KeyEvent.VK_DOWN, false)) {
                addViewLoc(0, -4);
            }
            if (keys.getOrDefault(KeyEvent.VK_RIGHT, false)) {
                addViewLoc(-4, 0);
            }
            // endregion
            if (time % 10 == 0) {
                if (hasjoy) {
                    if (joy.getComponents()[7].getPollData() == 1) {
                        root.info.draweditor = !root.info.draweditor;
                    }
                    if (Math.abs(joy.getComponents()[1].getPollData()) > 0.05
                            && Math.abs(joy.getComponents()[0].getPollData()) > 0.05)
                        addViewLoc(-20 * joy.getComponents()[1].getPollData() / zoom,
                                -20 * joy.getComponents()[0].getPollData() / zoom);
                    zoom *= Math.pow(2, -joy.getComponents()[4].getPollData() / 5);
                    tx = new AffineTransform();
                    tx.translate(root.getWidth() / 2, root.getHeight() / 2);
                    tx.translate(vx, vy);
                    tx.scale(zoom, zoom);
                }
                if (focus != null) {// move focused creature
                    focus.outputs[0] = keys.getOrDefault(KeyEvent.VK_W, false)
                            || (hasjoy && -joy.getComponents()[2].getPollData() > 0.6);
                    focus.outputs[1] = keys.getOrDefault(KeyEvent.VK_A, false)
                            || (hasjoy && -joy.getComponents()[3].getPollData() > 0.6);
                    focus.outputs[2] = keys.getOrDefault(KeyEvent.VK_S, false)
                            || (hasjoy && joy.getComponents()[2].getPollData() > 0.6);
                    focus.outputs[3] = keys.getOrDefault(KeyEvent.VK_D, false)
                            || (hasjoy && joy.getComponents()[3].getPollData() > 0.6);
                    focus.outputs[4] = keys.getOrDefault(KeyEvent.VK_SHIFT, false)
                            || (hasjoy && joy.getComponents()[9].getPollData() > 0.9);
                    focus.outputs[5] = keys.getOrDefault(KeyEvent.VK_SPACE, false)
                            || (hasjoy && joy.getComponents()[10].getPollData() > 0.9);
                    focus.outputs[6] = keys.getOrDefault(KeyEvent.VK_Q, false)
                            || (hasjoy && joy.getComponents()[12].getPollData() > 0.9);
                    focus.outputs[7] = keys.getOrDefault(KeyEvent.VK_E, false)
                            || (hasjoy && joy.getComponents()[11].getPollData() > 0.9);
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // region listeners
    @Override
    public void keyPressed(KeyEvent arg0) {
        int key = arg0.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) {
            if (focus != null) {
                if (root.info.draweditor)
                    root.info.draweditor = false;
                else {
                    focus.hasfocus = false;
                    focus = null;
                }
            } else
                System.exit(0);
        }
        if (key == KeyEvent.VK_ENTER)
            root.terminal.execute();
        else if (arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE)
            root.terminal.remchar();
        else if (arg0.getKeyChar() != '\uFFFF')
            root.terminal.type(arg0.getKeyChar());
        if (key == KeyEvent.VK_SLASH)
            root.terminal.activate();
        keys.put(key, true);
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        int key = arg0.getKeyCode();
        keys.put(key, false);
        if (key == KeyEvent.VK_K)
            root.tree.KILL();
        if (key == KeyEvent.VK_P)
            root.paused = !root.paused;

    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (root.info.c != null
                && !(root.info.scale().contains(e.getPoint()) || root.info.scaleEdit().contains(e.getPoint()))
                || root.info.c == null) {
            if (focus != null) {
                focus.hasfocus = false;
                focus = null;
            }
            Creature[] checkC = root.safeclone.get(Creature.class).toArray(new Creature[0]);
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
        if (root.info.c != null
                && !(root.info.scale().contains(e.getPoint()) || root.info.scaleEdit().contains(e.getPoint()))
                || root.info.c == null) {
            vx2 = e.getX();
            vy2 = e.getY();
        } else {
            root.info.click(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (root.info.c != null
                && !(root.info.scale().contains(e.getPoint()) || root.info.scaleEdit().contains(e.getPoint()))
                || root.info.c == null) {
            addViewLoc(e.getX() - vx2, e.getY() - vy2);
            vx2 = e.getX();
            vy2 = e.getY();
        } else {
            root.info.release(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (root.info.c != null
                && !(root.info.scale().contains(e.getPoint()) || root.info.scaleEdit().contains(e.getPoint()))
                || root.info.c == null) {
            addViewLoc((e.getX() - vx2), (e.getY() - vy2));
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
        if (e.isControlDown()) {
            zoom(Math.pow(2, -e.getPreciseWheelRotation()));
            setViewLoc(-vx * Math.pow(2, -e.getPreciseWheelRotation()), vy * Math.pow(2, -e.getPreciseWheelRotation()));
        } else {
            double factor = 10;
            if (e.isShiftDown())
                addViewLoc(-factor*e.getPreciseWheelRotation(), 0);
            else
                addViewLoc(0, -factor*e.getPreciseWheelRotation());

        }
    }

    // endregion
    public void setViewLoc(double newvx, double newvy) {
        vx = -newvx;
        vy = newvy;
        resetTransform();
    }

    public void addViewLoc(double addvx, double addvy) {
        vx += addvx;
        vy += addvy;
        resetTransform();
    }

    public void zoom(double factor) {
        zoom *= factor;
        resetTransform();
    }

    public void setzoom(double factor) {
        zoom = factor;
        resetTransform();
    }

    public void resetTransform() {
        tx = new AffineTransform();
        tx.translate(root.getWidth() / 2, root.getHeight() / 2);
        tx.translate(vx, vy);
        tx.scale(zoom, zoom);
    }

    void initcontrol() {
        Controller[] control = ControllerEnvironment.getDefaultEnvironment().getControllers();
        joy = null;
        Component[] components = null;
        hasjoy = false;
        for (int i = 0; i < control.length; i++) {// print components
            // System.out.println(control[i].getName());
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