package command;

import managers.QuadTree;
import managers.ControlManager;

public abstract class Command {
    String name;
    ControlManager controls;
    Command(String name, ControlManager controls){
        this.name = name;
        this.controls = controls;
    }
    public abstract String execute(String[] params, QuadTree tree);
    public String get(){
        return name;
    }
}