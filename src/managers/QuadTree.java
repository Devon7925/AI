package managers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import calc.Settings;
import geom.Point;
import geom.Rectangle;
import geom.Shape;
import sprites.Creature;
import sprites.Sprite;

public class QuadTree implements Cloneable {
    QuadTree[][] children;
    QuadTree parent;
    HashSet<Sprite> sprites;
    public HashSet<Sprite> queue;
    boolean split;

    public QuadTree() {
        sprites = new HashSet<Sprite>(Settings.bucket);
        queue = new HashSet<Sprite>();
        split = false;
    }

    public QuadTree(QuadTree q) {
        if (q.sprites != null)
            sprites = new HashSet<Sprite>(q.sprites);
        else
            sprites = new HashSet<Sprite>(Settings.bucket);
        split = q.split;
        if (split) {
            children = new QuadTree[2][2];
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    if (q.children[i][i1] != null)
                        children[i][i1] = new QuadTree(q.children[i][i1]);
                    else
                        children[i][i1] = new QuadTree();
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
                    for (Sprite s : children[i][i1].sprites) {
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
        sprites.forEach(s -> s.run());
        sprites.addAll(queue);
        queue.clear();
        sprites.removeIf(s -> s.dead);
        sprites.stream().filter(n -> n instanceof Creature).map(n -> (Creature) n).forEach(n -> n.runNet());
        runForNodesIfSplit(n -> n.runSP());
    }

    public int count() {
        return get().size();
    }

    public HashSet<Sprite> get() {
        HashSet<Sprite> fod = new HashSet<Sprite>(sprites);
        runForNodesIfSplit(n -> fod.addAll(n.get()));
        return fod;
    }

    public <T extends Sprite> int count(Class<T> clazz) {
        int c = 0;
        for (Sprite s : sprites) {
            if (clazz.isInstance(s))
                c++;
        }
        c += mapForNodesIfSplit(n -> n.count(clazz)).collect(Collectors.summingInt(n->n));
        return c;
    }

    public <T extends Sprite> ArrayList<T> get(Class<T> clazz) {
        ArrayList<T> fod = new ArrayList<T>();
        Sprite[] loopS = sprites.toArray(new Sprite[0]);
        for (Sprite s : loopS) {
            if (clazz.isInstance(s))
                fod.add(clazz.cast(s));
        }
        runForNodesIfSplit(n -> fod.addAll(n.get(clazz)));
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
            return false;
        } else
            return b;
    }

    public <T extends Sprite> boolean removeAll(HashSet<T> s) {
        boolean sucess = false;
        for (Sprite sprite : s) {
            if (remove(sprite))
                sucess = true;
        }
        return sucess;
    }

    public void addAll(HashSet<Sprite> s) {
        s.forEach(sp -> add(sp));
    }

    public void draw(Graphics2D g2, final Rectangle r) {
        runForNodesIfSplit((i, i1) -> children[i][i1].draw(g2, r.corner(i,i1)));
        g2.setColor(Color.BLACK);
        r.draw(g2);
        get().forEach(s -> s.draw(g2));
    }

    public void drawtree(Graphics2D g2, final Rectangle r) {
        if (split) {
            for (int i = 0; i < children.length; i++) {
                for (int i1 = 0; i1 < children[i].length; i1++) {
                    children[i][i1].drawtree(g2, r.corner(i, i1));
                }
            }
        } else
            r.draw(g2);
    }

    public HashSet<Sprite> query(Shape s, Rectangle recur) {
        if (recur.intersects(s)) {
            HashSet<Sprite> rectSprites = new HashSet<Sprite>(sprites);
            runForNodesIfSplit((i, i1) -> rectSprites.addAll(children[i][i1].query(s, recur.corner(i, i1))));
            return rectSprites;
        }
        return new HashSet<Sprite>();
    }

    public QuadTree parent() {
        return parent == null ? this : parent.parent();
    }

    public void KILL() {
        Sprite[] loopS = sprites.toArray(new Sprite[0]);
        for (Sprite s : loopS)
            remove(s);
        runForNodesIfSplit(n -> n.KILL());
    }

    public void runForNodesIfSplit(Consumer<QuadTree> toRun) {
        runForNodesIfSplit((i, i1) -> toRun.accept(children[i][i1]));
    }

    public void runForNodesIfSplit(BiConsumer<Integer, Integer> toRun) {
        if (split)
            for (int i = 0; i < children.length; i++)
                for (int i1 = 0; i1 < children[i].length; i1++)
                    toRun.accept(i, i1);
    }

    public <T> Stream<T> mapForNodesIfSplit(BiFunction<Integer, Integer, T> toRun) {
        Builder<T> builder = Stream.builder();
        if (split)
            for (int i = 0; i < children.length; i++)
                for (int i1 = 0; i1 < children[i].length; i1++)
                    builder.accept(toRun.apply(i, i1));
        return builder.build();
    }

    public <T> Stream<T> mapForNodesIfSplit(Function<QuadTree, T> toRun) {
        return mapForNodesIfSplit((i, i1) -> toRun.apply(children[i][i1]));
    }
}