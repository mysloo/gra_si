package main;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Target extends Rectangle {
    public Target() { super(); }
    public Target(double x, double y, double w, double h, Color c){
        super(x, y, w, h);
        this.setFill(c);
    }
}
