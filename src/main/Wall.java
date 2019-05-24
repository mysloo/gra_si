package main;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Wall extends Rectangle {
    public Wall() { super(); }
    public Wall(double x, double y, double w, double h, Color c){
        super(x, y, w, h);
        this.setFill(c);
    }
}
