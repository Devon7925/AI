package gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

import command.Command;
import command.Find;
import command.Goto;
import managers.ControlManager;
import managers.QuadTree;

public class Terminal {
    boolean active = false;
    final ArrayList<String> history = new ArrayList<String>();
    String command = "";
    Rectangle2D bounds;
    final QuadTree tree;
    final ArrayList<Command> commands;

    public Terminal(Rectangle2D bounds, QuadTree tree, ControlManager controls) {
        this.bounds = bounds;
        this.tree = tree;
        commands = new ArrayList<>(Arrays.asList(new Find(controls), new Goto(controls)));
    }

    public void type(char c) {
        if (active)
            command += c;
    }

    public void remchar() {
        command = command.substring(0, command.length() - 1);
    }

    public void execute() {
        String[] params = command.split(" ");
        if (params.length > 0) {
            history.add(command);
            history.add(commands.stream().filter(n -> n.get().equals(params[0])).findAny().get().execute(params, tree));
        }
        command = "";
        active = false;
    }

    public void draw(Graphics2D g2) {
        Drawer d = new Drawer(bounds, g2);
        d.setColor(Color.BLACK);
        for (int i = 0; i < history.size(); i++) {
            d.setColor(d.colour, (int) (127 * Math.pow(0.9, history.size() - i - 1)));
            d.drawString(history.get(i), 0.5, 0.8 + (i - history.size()) / 40.0);
        }
        d.setColor(Color.BLACK);
        d.drawString(command, 0.5, 0.9);
    }

    public void activate() {
        active = true;
    }

    public void update(Rectangle2D bounds) {
        this.bounds = bounds;
    }
}