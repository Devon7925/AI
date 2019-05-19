package brain;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;

import calc.Arith;
import calc.Settings;
import gui.Drawer;

public class Network {
    public final ArrayList<NOR> nodes;
    final HashSet<NOR> toUpdate;

    public Network() {
        nodes = new ArrayList<>();
        toUpdate = new HashSet<>();
        for (int i = 0; i < Settings.inputcount; i++)
            nodes.add(new NOR(true, new Short[0], 0.1f, 0.02f * i + 0.009f, this));
        for (int i = 0; i < Settings.outputcount; i++)
            nodes.add(new NOR(false, new Short[0], 0.9f, 0.36f + 0.02f * i, this));
    }

    public void change(int factor) {
        do
            mutate();
        while ((new Random()).nextInt(factor) > 0);
    }

    public void run(boolean[] in) {
        for (int i = 0; i < in.length; i++) {
            nodes.get(i).nextval = in[i];
            toUpdate.add(nodes.get(i));
        }
        run();
    }

    public void run() {
        toUpdate.removeIf(n -> n.run());
        toUpdate.forEach(n -> n.val = n.nextval);
        toUpdate.clear();
        toUpdate.addAll(
                toUpdate.stream().flatMap(n -> n.out.stream()).distinct().map(nodes::get).collect(Collectors.toList()));
    }

    public boolean[] giveOutput() {
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
        for (NOR node : drawnor) {// draw nodes
            d.setColor(Color.RED, 150);
            if (node.val)
                d.setColor(Color.GREEN, 150);
            d.drawRect(new Rectangle2D.Double(node.x - 0.005, node.y - 0.009, 0.01, 0.018));
            for (Short s : node.in) {
                d.setColor(Color.BLACK, 150);
                d.drawLine(node.x, node.y, nodes.get(s).x, nodes.get(s).y, Color.BLUE, 150);
            }
        }
    }

    public void remove(int id) {
        nodes.forEach(node -> node.in.removeIf(n -> n == id));
        for (NOR node : nodes) {
            node.in.stream().map(n -> n = (short) (n + (n > id ? -1 : 0)));
            node.out.stream().map(n -> n = (short) (n + (n > id ? -1 : 0)));
        }
        nodes.remove(id);
    }

    public void mutate() {
        Random rand = new Random();
        int rnd = rand.nextInt(100);
        int threshhold = Settings.remprob;
        if (rnd < threshhold) // remove
            if (nodes.size() > Settings.outputcount + Settings.inputcount) {
                remove(rand.nextInt(nodes.size() - Settings.outputcount - Settings.inputcount) + Settings.outputcount
                        + Settings.inputcount);
                return;
            }

        threshhold += Settings.addprob;
        if (rnd < threshhold) {// add
            ArrayList<Short> in = new ArrayList<Short>();
            short t = 1;
            do {
                short temp = (short) rand.nextInt(nodes.size() - Settings.outputcount);
                temp = (short) (temp + (temp > Settings.inputcount ? Settings.outputcount : 0));
                in.add(temp);
                t++;
            } while (rand.nextInt(t) == 0);
            NOR toAdd = new NOR(false, Arith.convertToArrayShort(in), rand.nextFloat(), rand.nextFloat(), this);
            nodes.add(toAdd);
            toAdd.in.forEach(n -> nodes.get(n).out.add((short) (nodes.size() - 1)));
            return;
        }

        // modify
        short index = (short) (rand.nextInt(nodes.size() - Settings.inputcount) + Settings.inputcount);
        do {
            short rnd2 = (short) rand.nextInt(100);
            if (rnd2 < 10 && nodes.get(index).in.size() > 1) {// remove
                HashSet<Short> torem = new HashSet<>();
                for (Short s : nodes.get(index).in)
                    if (rand.nextBoolean()) {
                        nodes.get(s).out.remove(index);
                        torem.add(s);
                    }
                nodes.get(index).in.removeAll(torem);
            } else {// add
                short temp = (short) rand.nextInt(nodes.size() - Settings.outputcount);
                temp = (short) (temp + (temp > Settings.inputcount ? Settings.outputcount : 0));
                nodes.get(index).in.add(temp);
                nodes.get(temp).out.add(index);
            }
        } while (rand.nextInt(5) == 0);
    }
}