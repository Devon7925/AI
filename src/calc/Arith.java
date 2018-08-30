package calc;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import brain.NOR;
import brain.Network;
import sprites.Bullet;
import sprites.Creature;
import sprites.Food;
import sprites.Sprite;

public class Arith {
    static Random rnd = new Random();

    public static Rectangle2D scale(Rectangle2D in, double scale) {
        return new Rectangle2D.Double(in.getX() + (in.getWidth() * (1 - scale)) / 2,
                in.getY() + (in.getHeight() * (1 - scale)) / 2, in.getWidth() * scale, in.getHeight() * scale);
    }

    public static Rectangle2D scale(Rectangle2D in, double scalex, double scaley) {
        return new Rectangle2D.Double(in.getX() + (in.getWidth() * (1 - scalex)) / 2,
                in.getY() + (in.getHeight() * (1 - scaley)) / 2, in.getWidth() * scalex, in.getHeight() * scaley);
    }

    /**
     * returns a shifted version of in
     */
    public static Rectangle2D shift(Rectangle2D in, double shiftx, double shifty) {
        return new Rectangle2D.Double(in.getX() + shiftx, in.getY() + shifty, in.getWidth(), in.getHeight());
    }

    /**
     * returns the whether s1 is closer to ref than s2
     * 
     * @param ref the reference for comparing distances
     */
    public static boolean closer(Sprite ref, Sprite s1, Sprite s2) {
        return dist(ref, s1) < dist(ref, s2);
    }

    /**
     * returns the whether s1 is closer to ref than s2 in a safe way
     * 
     * @param ref the reference for comparing distances
     */
    public static boolean safecloser(Sprite ref, Sprite s1, Sprite s2) {
        int type = Arith.TypeToID(s1.getClass());
        switch (type) {
        case 0:
            return false;
        case 1:
        case 2:
        case 3:
            return closer(ref, s1, s2);
        }
        return false;
    }

    /**
     * finds distance from origin to (x, y)
     */
    public static double dist(double x, double y) {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    /**
     * finds the distance between two sprites
     */
    public static double dist(Sprite s1, Sprite s2) {
        return dist(s1.getX() - s2.getX(), s1.getY() - s2.getY());
    }

    /**
     * converts a integer arraylist to a integer array
     */
    public static Integer[] convertToArrayInteger(List<Integer> list) {
        Integer[] ret = new Integer[list.size()];
        Iterator<Integer> iterator = list.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }

    /**
     * converts a short arraylist to a short array
     */
    public static Short[] convertToArrayShort(List<Short> list) {
        Short[] ret = new Short[list.size()];
        Iterator<Short> iterator = list.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next().shortValue();
        }
        return ret;
    }

    /**
     * returns a number representing the type of Sprite
     */
    public static <T extends Sprite> int TypeToID(Class<T> s) {
        if (s == Creature.class)
            return 1;
        if (s == Food.class)
            return 2;
        if (s == Bullet.class)
            return 3;
        return 0;
    }

    /**
     * finds the average color
     */
    public static Color mix(Color a, Color b) {
        return new Color((a.getRGB() + b.getRGB()) / 2);
    }

    /**
     * Creates a combined version of a & b
     */
    public static Network mix(Network a, Network b) {
        Network mix = new Network();
        int indexa = 0, indexb = 0;
        while (true) {
            if (rnd.nextBoolean() && indexa < a.nodes.size()) {
                mix.nodes.add(a.nodes.get(indexa));
            } else if (indexb < b.nodes.size()) {
                mix.nodes.add(b.nodes.get(indexb));
            }
            if (rnd.nextInt(40) == 0) {
                if (rnd.nextBoolean()) {
                    indexa++;
                } else {
                    indexb++;
                }
            }
            indexa++;
            indexb++;
            if (indexa >= a.nodes.size() && indexb >= b.nodes.size()) {
                for (NOR node : mix.nodes) {
                    if (node.in.size() > 0) {
                        for (int i = 0; i < node.in.size(); i++) {
                            if (node.in.get(i) >= mix.nodes.size()) {
                                node.in.remove(i);
                                i--;
                            }
                        }
                    }
                }
                return mix;
            }
        }
    }

    /**
     * returns a version of s that is in the range
     */
    public static Sprite range(Sprite s) {
        if (s.getX() > Settings.range)
            s.move(Settings.range - s.getX() - 1, 0);
        else if (s.getX() < -Settings.range)
            s.move(-Settings.range - s.getX() + 1, 0);
        if (s.getY() > Settings.range)
            s.move(0, Settings.range - s.getY() - 1);
        else if (s.getY() < -Settings.range)
            s.move(0, -Settings.range - s.getY() + 1);
        return s;
    }

    /**
     * returns all Sprites of a given type in s
     * 
     * @param clazz the type to return
     * @param s     the dataset
     */
    public static <T extends Sprite> Vector<T> filter(Class<T> clazz, Vector<Sprite> s) {
        Vector<T> fod = new Vector<T>(s.size() / 2);
        s.trimToSize();
        Sprite[] loopS = s.toArray(new Sprite[0]);
        for (Sprite sp : loopS) {
            if (clazz.isInstance(sp))
                fod.add(clazz.cast(sp));
        }
        return fod;
    }

    /**
     * Returns the largest of two integers
     */
    public static double max(double a, double b) {
        return a > b ? a : b;
    }

    /**
     * Returns the smallest of two integers
     */
    public static double min(double a, double b) {
        return a > b ? b : a;
    }
}