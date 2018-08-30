package managers;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Vector;

import calc.Settings;
import geom.Line;
import geom.Point;
import geom.Rectangle;
import sprites.Sprite;

public class QuadTree implements Cloneable{
    QuadTree[][] children;
    QuadTree parent;
    Vector<Sprite> sprites;
    boolean split;

    public QuadTree() {
        sprites = new Vector<Sprite>(Settings.bucket);
        split = false;
    }

    public QuadTree(QuadTree q) {
        if(q.sprites != null) sprites = new Vector<Sprite>(q.sprites);
        else sprites = new Vector<Sprite>(Settings.bucket);
        split = q.split;
        if(split){
            children = new QuadTree[2][2];
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    if(q.children[i][i1] != null) children[i][i1] = new QuadTree(q.children[i][i1]);
                    else children[i][i1] = new QuadTree();
                    children[i][i1].parent = this;
                }
            }
        }
    }

    public void reorg() {
        while (!reorg2())
            ;
    }

    private boolean reorg2() {
        boolean done = true;
        if (count() >= Settings.bucket || split) {
            if (!split) {
                children = new QuadTree[2][2];
                split = true;
                for (int i = 0; i < children.length; i++) {
                    for (int i1 = 0; i1 < children[i].length; i1++) {
                        children[i][i1] = new QuadTree();
                        children[i][i1].parent = this;
                    }
                }
            }
            sprites.trimToSize();
            Sprite[] loopS = sprites.toArray(new Sprite[0]);
            for (Sprite s : loopS) {
                if (s.innerloc.getX() < 1 && s.innerloc.getY() < 1 && s.innerloc.getX() > 0 && s.innerloc.getY() > 0) {
                    children[(int) Math.floor(s.innerloc.getX() * children.length)][(int) Math
                            .floor(s.innerloc.getY() * children[0].length)].sprites.add(s);
                    s.innerloc = new Point(2 * (s.innerloc.getX() % (0.5)), 2 * (s.innerloc.getY() % (0.5)));
                    s.range /= 2;
                    remove(s);
                }
            }
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    children[i][i1].reorg();
                    sprites.trimToSize();
                    Sprite[] loopS1 = children[i][i1].sprites.toArray(new Sprite[0]);
                    for (Sprite s : loopS1) {
                        if (s.innerloc.getX() > 1 || s.innerloc.getY() > 1 || s.innerloc.getX() < 0
                                || s.innerloc.getY() < 0) {
                            s.innerloc = new Point((i + s.innerloc.getX()) / 2, (i1 + s.innerloc.getY()) / 2);
                            s.range *= 2;
                            remove(s);
                            sprites.add(s);
                            done = false;
                        }
                    }
                }
            }
        }
        if (count() < Settings.bucket && split) {
            split = false;
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    for(Sprite s : children[i][i1].sprites){
                        s.innerloc = new Point((i + s.innerloc.getX()) / 2, (i1 + s.innerloc.getY()) / 2);
                        s.range *= 2;
                        add(s);
                    }
                }
            }
            children = null;
            done = false;
        }
        return done;
    }

    public void runSP() {
        Sprite[] run = sprites.toArray(new Sprite[0]);
        for (Sprite s : run) {
            s.run();
        }
        if (split) {
            for (QuadTree[] q1 : children) {
                for (QuadTree q : q1) {
                    q.runSP();
                }
            }
        }
    }

    public int count() {
        return get().size();
    }

    public ArrayList<Sprite> get() {
        ArrayList<Sprite> fod = new ArrayList<Sprite>();
        Sprite[] loopS = sprites.toArray(new Sprite[0]);
        for (Sprite s : loopS) {
            fod.add(s);
        }
        if (split) {
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    fod.addAll(children[i][i1].get());
                }
            }
        }
        return fod;
    }

    public <T extends Sprite> int count(Class<T> clazz) {
        int c = 0;
        Sprite[] loopS = sprites.toArray(new Sprite[0]);
        for (Sprite s : loopS) {
            if (clazz.isInstance(s))
                c++;
        }
        if (split) {
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    c += children[i][i1].count(clazz);
                }
            }
        }
        return c;
    }

    public <T extends Sprite> ArrayList<T> get(Class<T> clazz) {
        ArrayList<T> fod = new ArrayList<T>();
        Sprite[] loopS = sprites.toArray(new Sprite[0]);
        for (Sprite s : loopS) {
            if (clazz.isInstance(s))
                fod.add(clazz.cast(s));
        }
        if (split) {
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    fod.addAll(children[i][i1].get(clazz));
                }
            }
        }
        return fod;
    }

    public void add(Sprite s) {
        s.holder = parent();
        sprites.add(s);
    }

    public boolean remove(Sprite s) {
        boolean b = sprites.remove(s);
        if (split && !b) {
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    b = children[i][i1].remove(s);
                    if (b)
                        return true;
                }
            }
        } else
            return b;
        return false;
    }

    public <T extends Sprite> boolean removeAll(ArrayList<T> s) {
        boolean sucess = false;
        for (Sprite sprite : s) {
            if (remove(sprite))
                sucess = true;
        }
        return sucess;
    }

    public void addAll(ArrayList<Sprite> s) {
        for (Sprite sprite : s) {
            add(sprite);
        }
    }

    public void draw(Graphics2D g2, final Rectangle r) {
        if (split) {
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    children[i][i1].draw(g2, r.corner(i, i1));
                }
            }
        }
        g2.setColor(Color.BLACK);
        r.draw(g2);
        for (Sprite s : get()) {
            s.draw(g2);
        }
    }

    public void drawtree(Graphics2D g2, final Rectangle r) {
        if (split) {
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    children[i][i1].drawtree(g2, r.corner(i, i1));
                }
            }
        }else r.draw(g2);
    }
    
    public Vector<Sprite> query(Rectangle r, Rectangle recur) {
        Vector<Sprite> fod = new Vector<Sprite>();
        Sprite[] loopS = sprites.toArray(new Sprite[0]);
        if(recur.intersects(r) || r.intersects(recur)){
            for (Sprite s : loopS) {
                if(r.contains(s.loc))fod.add(s);
            }
            if (split) {
                for (int i = 0; i < children.length; i++) {
                    for (int i1 = 0; i1 < children[i].length; i1++) {
                        fod.addAll(children[i][i1].query(r, recur.corner(i, i1)));
                    }
                }
            }
        }
        return fod;
    }

    public Vector<Sprite> query(Line l, Rectangle recur) {
        Vector<Sprite> fod = new Vector<Sprite>();
        if(recur.intersects(l)){
            Sprite[] loopS = sprites.toArray(new Sprite[0]);
            for (Sprite s : loopS) {
                fod.add(s);
            }
            if (split) {
                for (int i = 0; i < children.length; i++) {
                    for (int i1 = 0; i1 < children[i].length; i1++) {
                        fod.addAll(children[i][i1].query(l, recur.corner(i, i1)));
                    }
                }
            }
        }
        return fod;
    }

    public QuadTree parent(){
        if(parent == null) return this;
        else return parent.parent();
    }
    
    public void KILL() {
        Sprite[] loopS = sprites.toArray(new Sprite[0]);
        for (Sprite s : loopS) {
            remove(s);
        }
        if(split){
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    children[i][i1].KILL();
                }
            }
        }
    }
}