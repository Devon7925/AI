package command;

import java.util.Comparator;

import managers.ControlManager;
import managers.QuadTree;
import sprites.Creature;

public class Goto extends Command{
    public Goto(ControlManager controls){
        super("goto", controls);
    }

	@Override
	public String execute(String[] params, QuadTree tree) {
		Creature c = null;
		if(params[1].equals(">"))c = tree.get(Creature.class).stream().max(new Sortbyname()).get();
        else if(params[1].equals("<")) c = tree.get(Creature.class).stream().min(new Sortbyname()).get();
        else {
            controls.setViewLoc(Integer.parseInt(params[1]), Integer.parseInt(params[2]));
            if(params.length > 3){
                controls.setzoom(Integer.parseInt(params[3]));
            }
            return "";
        }
        controls.setViewLoc(c.getX()*controls.zoom, c.getY()*controls.zoom);
		return c.getX()+", "+c.getY()+"--"+c.getAge();
	}
 
	class Sortbyname implements Comparator<Creature>
	{
		// Used for sorting in ascending order of
		// roll name
		public int compare(Creature a, Creature b)
		{
			return a.getAge()-b.getAge();
		}
	}
}