package main;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Scanner;


public class Missile extends Circle {
    private double velocity, degrees;
    private double shiftX, shiftY;
    private long start, end;
    public Missile(){ super(); }
    public Missile(double x, double y, double r, Color c){
        super(x, y, r, c);
        init();
    }

    public void move(){
        this.setCenterX(this.getCenterX() + shiftX);
        this.setCenterY(this.getCenterY() + shiftY);
        if(shiftX != 0){
            shiftY += 0.1;
        }
    }
    public void init(){
        this.setCenterX(120);
        this.setCenterY(630);
        Game.timeline.stop();

        System.out.println("Podaj");
        Scanner sc = new Scanner(System.in);
        this.setVelocity(sc.nextDouble());
        this.setDegrees(sc.nextDouble());
        start = System.currentTimeMillis();
        //this.setVelocity(Math.random()*25+1.0);
        //this.setDegrees(Math.random()*90+1.0);
        Game.timeline.play();
    }
    public int checkCollision(Wall wall){
        if(this.getCenterY() > Game.APP_H){
            end = System.currentTimeMillis();
            System.out.println("ziemia ");
            System.out.println(this.getCenterX()-Game.target.getX());
            System.out.println((end-start)/1000F);
            init();
            return 1;
        }
        if(wall.getX()+wall.getWidth() >= this.getCenterX() && wall.getX() <= this.getCenterX()
                && wall.getY() <= this.getCenterY()){
            end = System.currentTimeMillis();
            System.out.println("sciana");
            System.out.println(this.getCenterX()-Game.target.getX());
            System.out.println((end-start)/1000F);
            init();
            return 2;
        }
        return 0;
    }
    public boolean reachedGoal(Target target){
        if(target.getX()+target.getWidth() >= this.getCenterX() && target.getX() <= this.getCenterX()
                    && target.getY() <= this.getCenterY()){
            end = System.currentTimeMillis();
            System.out.println("Sukces");
            System.out.println(this.getCenterX()-Game.target.getX());
            System.out.println((end-start)/1000F);
            init();
            return true;
        }
        return false;
    }



    public double getShiftX() {
        return shiftX;
    }
    public void setShiftX(double shiftX) {
        this.shiftX = shiftX;
    }
    public double getShiftY() {
        return shiftY;
    }
    public void setShiftY(double shiftY) {
        this.shiftY = shiftY;
    }
    public double getVelocity() {
        return velocity;
    }
    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }
    public double getDegrees() {
        return degrees;
    }
    public void setDegrees(double degrees) {
        this.degrees = degrees;
        double radians = Math.toRadians(degrees);
        shiftX = velocity*Math.cos(radians);
        shiftY = velocity*Math.sin(radians) * -1;
    }
}
