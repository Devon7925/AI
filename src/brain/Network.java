package brain;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

import calc.Arith;
import calc.Settings;
import gui.Drawer;

public class Network {
    public ArrayList<NOR> nodes;
    ArrayList<NOR> toUpdate;

    public Network() {
        nodes = new ArrayList<>();
        toUpdate = new ArrayList<>();
        for (int i = 0; i < Settings.inputcount; i++) {
            nodes.add(new NOR(false, new Short[0], 0.1, 0.02 * i + 0.009, this));
        }
        for (int i = 0; i < Settings.outputcount; i++) {
            nodes.add(new NOR(false, new Short[0], 0.9, 0.36 + 0.02 * i, this));
        }
    }

    public void change(int factor){
        do {
            mutate();
        } while ((new Random()).nextInt(factor) > 0);
    }

    public void run(boolean[] in) {
        for (int i = 0; i < in.length; i++) {
            nodes.get(i).nextval=in[i];
            toUpdate.add(nodes.get(i));
        }
        run();
    }

    public void run() {
        toUpdate.forEach(n -> n.run());
        ArrayList<NOR> newtoUpdate = new ArrayList<>();
        nodes.stream().filter(n -> n.val != n.nextval).forEach(n -> newtoUpdate.addAll(n.run2(newtoUpdate)));
        toUpdate = newtoUpdate;
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

    public void remove(int id) {
        for (NOR node : nodes) {
            node.in.removeIf(n -> n == id);
        }
        for (NOR node : nodes) {
            for (int i1 = 0; i1 < node.in.size(); i1++) {
                node.in.set(i1, (short)(node.in.get(i1) + ((node.in.get(i1) > id) ? -1 : 0)));
            }
        }
        nodes.remove(id);
    }

    public void mutate() {
        Random rand = new Random();
        boolean fail = false;
        do {
            fail = false;
            int rnd = rand.nextInt(100);
            if (rnd < Settings.remprob) {//remove
                if (nodes.size() <= Settings.outputcount + Settings.inputcount)
                    fail = true;
                else
                    remove(rand.nextInt(nodes.size() - Settings.outputcount - Settings.inputcount) + Settings.outputcount + Settings.inputcount);
            } else if (rnd < Settings.remprob+Settings.addprob) {//add
                ArrayList<Short> in = new ArrayList<Short>();
                short t = 1; 
                do {
                    short temp = (short) rand.nextInt(nodes.size()-Settings.outputcount);
                    temp = (short)(temp+(temp>Settings.inputcount?Settings.outputcount:0));
                    in.add(temp);
                    t++;
                } while (rand.nextInt(t) == 0);
                NOR toAdd = new NOR(false, Arith.convertToArrayShort(in), rand.nextDouble(), rand.nextDouble(), this);
                nodes.add(toAdd);
                toAdd.in.forEach(n -> nodes.get(n).out.add((short) nodes.indexOf(toAdd)));

            } else {//modify
                short index = (short) (rand.nextInt(nodes.size()-Settings.inputcount)+Settings.inputcount);
                do {
                    short rnd2 = (short) rand.nextInt(100);
                    if (rnd2 < 10 && nodes.get(index).in.size() > 1) {//remove
                        nodes.get(index).in.remove(rand.nextInt(nodes.get(index).in.size()));
                    } else {//add
                        short temp = (short) rand.nextInt(nodes.size()-Settings.outputcount);
                        temp = (short)(temp+(temp>Settings.inputcount?Settings.outputcount:0));
                        nodes.get(index).in.add(temp);
                        nodes.get(temp).out.add(index);
                    }
                } while (rand.nextInt(5)==0);
            }
        } while (fail);
    }
}