import java.awt.Color;
import java.awt.geom.Rectangle2D;

class Button{
    String content;
    Color color;
    Rectangle2D bounds;
    public Button(String s, Color c, Rectangle2D r){
        content = s;
        color = c;
        bounds = r;
    }
    void draw(Drawer d){
        d.setColor(color);
        d.drawRect(bounds);
        d.setColor(Color.BLACK);
        d.drawString(content, bounds.getCenterX(), bounds.getCenterY());
    }
}