import java.util.ArrayList;
import java.util.Arrays;

public class NOR {
	boolean val;
	boolean nextval;
	ArrayList<Integer> in;
	double x, y;
	public NOR(boolean val1, Integer[] in1, double x1, double y1) {
		val = val1;
		in = new ArrayList<>(Arrays.asList(in1));
		x = x1;
		y = y1;
	}
	void run() {
		val = nextval;
	}
	void setval(boolean b){
		val = b;
		nextval = b;
	}
}
