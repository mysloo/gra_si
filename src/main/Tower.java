package main;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tower extends Rectangle {
    public Tower(){super();}
    public Tower(double x, double y, double w, double h, Color c){
        super(x, y, w, h);
        this.setFill(c);
    }
}
