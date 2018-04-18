import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

class Network {
    ArrayList<NOR> nodes;

    public Network() {
        nodes = new ArrayList<>();
        for (int i = 0; i < Settings.inputcount; i++) {
            nodes.add(new NOR(false, new Integer[0], 0.1, 0.02 * i + 0.009));
        }
        for (int i = 0; i < Settings.outputcount; i++) {
            nodes.add(new NOR(false, new Integer[0], 0.9, 0.36 + 0.02 * i));
        }
    }

    public void change(int factor){
        do {
            mutate();
        } while ((new Random()).nextInt(factor) > 0);
    }

    public void run(boolean[] in) {
        for (int i = 0; i < in.length; i++) {
            nodes.get(i).setval(in[i]);
        }
        run();
    }

    public void run() {
        for (NOR node : nodes) {
            if (node.in.size() != 0) {
                node.nextval = true;
                for (int i = 0; i < node.in.size(); i++) {
                    if (nodes.get(node.in.get(i)).val) {
                        node.nextval = false;
                        break;
                    }
                }
            }
        }
        for (NOR node : nodes) {
            node.run();
        }
    }

    boolean[] giveOutput() {
        boolean[] out = new boolean[Settings.outputcount];
        for (int i = 0; i < Settings.outputcount; i++) {
            out[i] = nodes.get(i + Settings.inputcount).val;
        }
        return out;
    }

    public void draw(Drawer d) {
        d.setColor(new Color(255, 255, 255, 200));
        d.drawRect(new Rectangle2D.Double(0, 0, 1, 1));
        NOR[] drawnor = nodes.toArray(new NOR[0]);
        for (NOR node : drawnor) {//draw nodes
            d.setColor(Color.RED, 150);
            if (node.val)
                d.setColor(Color.GREEN, 150);
            d.drawRect(new Rectangle2D.Double(node.x - 0.005, node.y - 0.009, 0.01, 0.018));
            for (int i1 = 0; i1 < node.in.size(); i1++) {
                d.setColor(Color.BLACK, 150);
                d.drawLine(node.x, node.y, nodes.get(node.in.get(i1)).x, nodes.get(node.in.get(i1)).y, Color.BLUE, 150);
            }
        }
    }

    void remove(int id) {
        for (NOR node : nodes) {
            for (int i1 = 0; i1 < node.in.size(); i1++) {
                if (node.in.get(i1) == id)
                    node.in.remove(i1);
            }
        }
        for (NOR node : nodes) {
            for (int i1 = 0; i1 < node.in.size(); i1++) {
                node.in.set(i1, node.in.get(i1) + ((node.in.get(i1) > id) ? -1 : 0));
            }
        }
        nodes.remove(id);
    }

    void mutate() {
        Random rand = new Random();
        boolean fail = false;
        do {
            fail = false;
            int rnd = rand.nextInt(100);
            if (rnd < 5) {//remove
                if (nodes.size() <= Settings.outputcount + Settings.inputcount)
                    fail = true;
                else
                    remove(rand.nextInt(nodes.size() - Settings.outputcount - Settings.inputcount) + Settings.outputcount + Settings.inputcount);
            } else if (rnd < 55) {//add
                ArrayList<Integer> in = new ArrayList<Integer>();
                do {
                    in.add(rand.nextInt(nodes.size()));
                } while (rand.nextInt(4) == 0);
                nodes.add(new NOR(false, Arith.convertIntegers(in), rand.nextDouble(), rand.nextDouble()));
            } else {//modify
                int index = rand.nextInt(nodes.size()-Settings.inputcount)+Settings.inputcount;
                do {
                    int rnd2 = rand.nextInt(100);
                    if (rnd2 < 10 && nodes.get(index).in.size() > 1) {//remove
                        nodes.get(index).in.remove(rand.nextInt(nodes.get(index).in.size()));
                    } else {//add
                        int r = rand.nextInt(nodes.size()-Settings.outputcount);
                        nodes.get(index).in.add(r>Settings.inputcount?Settings.outputcount:0+r);
                    }
                } while (rand.nextInt(5)==0);
            }
        } while (fail);
    }
}