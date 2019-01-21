package calc;
import geom.Point;
import geom.Rectangle;
import geom.Square;

public class Settings{
	public static final int 
		    range = 5000,
		    foodcap = range*range/3200,
		    creaturecap = range*range/3200/50,
			inputcount = 46,
			outputcount = 9,
			inPerView = 9,
			netPerCycle = 10,
			bucket = 4,
			startingcomplexity = 100,
			remprob = 5,
			addprob = 60;
    public static final double viewspread = Math.PI / 12;
    public static final Rectangle rangeRect = new Square(new Point(0, 0), 2*Settings.range);
}