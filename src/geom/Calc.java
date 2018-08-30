package geom;
public class Calc {
	public static boolean[] binary(int lp, int bitc) {
		boolean[] bits = new boolean[bitc];
		for (int i = bitc-1; i >= 0; i--) {
			bits[i] = (lp & (1 << i)) != 0;
		}
		return bits;
	}
	public static double dist(double a, double b) {
		return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
	}
	public static double quad1(double a, double b, double c){
		return (-b+Math.sqrt(Math.pow(b, 2)-4*a*c))/(2*a);
	}
	public static double quad2(double a, double b, double c){
		return (-b-Math.sqrt(Math.pow(b, 2)-4*a*c))/(2*a);
	}
}
