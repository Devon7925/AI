package command;

import java.util.Comparator;

import managers.ControlManager;
import managers.QuadTree;
import sprites.Creature;

public class Find extends Command{
    public Find(ControlManager controls){
        super("find", controls);
    }

	@Override
	public String execute(String[] params, QuadTree tree) {
		Creature c = null;
		if(params[1].equals(">"))c = tree.get(Creature.class).stream().max(new Sortbyname()).get();
		else c = tree.get(Creature.class).stream().min(new Sortbyname()).get();
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