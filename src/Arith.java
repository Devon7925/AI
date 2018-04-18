import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

class Arith {
    static Random rnd = new Random();

    static Rectangle2D scale(Rectangle2D in, double scale) {
        return new Rectangle2D.Double(in.getX() + (in.getWidth() * (1 - scale)) / 2,
                in.getY() + (in.getHeight() * (1 - scale)) / 2, in.getWidth() * scale, in.getHeight() * scale);
    }

    static Rectangle2D scale(Rectangle2D in, double scalex, double scaley) {
        return new Rectangle2D.Double(in.getX() + (in.getWidth() * (1 - scalex)) / 2,
                in.getY() + (in.getHeight() * (1 - scaley)) / 2, in.getWidth() * scalex, in.getHeight() * scaley);
    }

    static Rectangle2D shift(Rectangle2D in, double shiftx, double shifty) {
        return new Rectangle2D.Double(in.getX() + shiftx, in.getY() + shifty, in.getWidth(), in.getHeight());
    }

    static boolean closer(Sprite ref, Sprite s1, Sprite s2) {
        return dist(ref, s1) < dist(ref, s2);
    }

    static double dist(double x, double y) {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    static double dist(Sprite s1, Sprite s2) {
        return dist(s1.getX() - s2.getX(), s1.getY() - s2.getY());
    }

    public static Integer[] convertIntegers(List<Integer> integers) {
        Integer[] ret = new Integer[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }

    public static <T extends Sprite> int TypeToID(Class<T> s) {
        if (s == Creature.class)
            return 1;
        if (s == Food.class)
            return 2;
        if (s == Bullet.class)
            return 3;
        return 0;
    }

    public static Color mix(Color a, Color b) {
        return new Color((a.getRGB() + b.getRGB()) / 2);
    }

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

    public static Sprite range(Sprite s) {
        if (s.getX() > Settings.range)
            s.move(Settings.range - s.getX()-1, 0);
        else if (s.getX() < -Settings.range)
            s.move(-Settings.range - s.getX()+1, 0);
        if (s.getY() > Settings.range)
            s.move(0, Settings.range - s.getY()-1);
        else if (s.getY() < -Settings.range)
            s.move(0, -Settings.range - s.getY()+1);
        return s;
    }

    public static <T extends Sprite> Vector<T> filter(Class<T> clazz, Vector<Sprite> s) {
        Vector<T> fod = new Vector<T>(s.size()/2);
        s.trimToSize();
        Sprite[] loopS = s.toArray(new Sprite[0]);
        for (Sprite sp : loopS) {
            if (clazz.isInstance(sp))
                fod.add(clazz.cast(sp));
        }
        return fod;
    }
}