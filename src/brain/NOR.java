package brain;

import java.util.Arrays;
import java.util.HashSet;

public class NOR {
	boolean val, nextval;
	public final HashSet<Short> in;
	public final HashSet<Short> out;
	float x, y;
	final Network holder;
	public NOR(boolean val1, Short[] in1, float x1, float y1, Network holder) {
		val = val1;
		in = new HashSet<>(Arrays.asList(in1));
		out = new HashSet<>();
		x = x1;
		y = y1;
		this.holder = holder;
	}
	boolean run() {
		if(in.size() > 0) nextval = in.stream().noneMatch(i -> holder.nodes.get(i).val);
		return val == nextval;
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
	public void setX(float x) {
		this.x = x;
	}
	public void setY(float y) {
		this.y = y;
	}
}
