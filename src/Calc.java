public class Calc {
	public static boolean[] binary(int lp, int bits) {
		boolean[] output = new boolean[bits];
		int p = lp;
		for(int i = output.length-1; i >= 0; i--) {
			output[i] = (p >= Math.pow(2, i));
			if(p >= Math.pow(2, i)) p -= Math.pow(2, i);
		}
		return output;
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
