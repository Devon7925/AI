package brain;
import java.util.ArrayList;
import java.util.Arrays;

public class NOR {
	boolean val, nextval;
	public ArrayList<Short> in;
	ArrayList<Short> out;
	double x, y;
	Network holder;
	public NOR(boolean val1, Short[] in1, double x1, double y1, Network holder) {
		val = val1;
		in = new ArrayList<>(Arrays.asList(in1));
		out = new ArrayList<>();
		x = x1;
		y = y1;
		this.holder = holder;
	}
	void run() {
		if(in.size() > 0){
			nextval = true;
			if(in.stream().anyMatch(i -> holder.nodes.get(i).val)) nextval = false;
		}
	}
	ArrayList<NOR> run2(ArrayList<NOR> toUpdate) {
		ArrayList<NOR> newUpdates = new ArrayList<NOR>();
		out.removeIf(n -> n >= holder.nodes.size());
		out.stream().filter(i -> !toUpdate.contains(holder.nodes.get(i))).forEach(i -> newUpdates.add(holder.nodes.get(i)));
		val = nextval;
		return newUpdates;
	}
	void setval(boolean b){
		val = b;
		nextval = b;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public void setX(double x) {
		this.x = x;
	}
	public void setY(double y) {
		this.y = y;
	}
}
