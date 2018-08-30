package gui;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D; 
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class Drawer {
    Graphics2D g2;
    Rectangle2D bounds;
    Color colour;
    Drawer(Rectangle2D bounds, Graphics2D g2){
        this.g2 = g2;
        this.bounds = bounds;
    }
    public void drawRect(Rectangle2D r){
        g2.fillRect(transx(r.getX()), transy(r.getY()), transw(r.getWidth()), transh(r.getHeight()));
    }
    public void drawLine(double x, double y, double x1, double y1){
        g2.setStroke(new BasicStroke(5));
        g2.draw(new Line2D.Double(transx(x), transy(y), transx(x1), transy(y1)));
    }
    public void drawLine(double x, double y, double x1, double y1, Color c){
        g2.setStroke(new BasicStroke(5));
        g2.drawLine(transx(x), transy(y), transx(x)+(transx(x1)-transx(x))/2, transy(y)+(transy(y1)-transy(y))/2);
        g2.setColor(c);
        g2.drawLine(transx(x)+(transx(x1)-transx(x))/2, transy(y)+(transy(y1)-transy(y))/2, transx(x1), transy(y1));
        setColor(colour);
    }
    public void drawLine(double x, double y, double x1, double y1, Color c, int a){
        drawLine(x, y, x1, y1, new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
    }
    public void drawString(String str, double x, double y){
        FontMetrics metrics = g2.getFontMetrics(g2.getFont());
        Rectangle2D r = metrics.getStringBounds(str, g2);
        g2.drawString(str, (int) (transx(x)-r.getWidth()/2), (int) (transy(y)+metrics.getLeading()/2));
    }
    public void setColor(Color c){
        g2.setColor(c);
        colour = c;
    }
    public void setColor(Color c, int a){
        colour = new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
        g2.setColor(colour);
    }
    private int transx(double x){
        return (int) (bounds.getX()+x*bounds.getWidth());
    }
    private int transy(double y){
        return (int) (bounds.getY()+y*bounds.getHeight());
    }
    private int transw(double x){
        return (int) (x*bounds.getWidth());
    }
    private int transh(double y){
        return (int) (y*bounds.getHeight());
    }
    public void offset(int x, int y){
        bounds.setFrame(x+bounds.getX(), y+bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }
}