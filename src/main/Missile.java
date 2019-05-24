package main;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import pomocne.RealFunction;


public class Missile extends Circle {
    private double velocity, degrees;
    private double shiftX, shiftY;
    private long start, end;
    private double time, distance;
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

        start = System.currentTimeMillis();
        //this.setVelocity(Math.random()*25+1.0);
        //this.setDegrees(Math.random()*90+1.0);
        Game.timeline.play();
    }
    public boolean checkCollision(Wall wall){
        if(this.getCenterY() > Game.APP_H){
            end = System.currentTimeMillis();
            time = (end-start)/10F;
            distance = this.getCenterX()- RealFunction.target.getX();
            init();
            return true;
        }
        if(wall.getX()+wall.getWidth() >= this.getCenterX() && wall.getX() <= this.getCenterX()
                && wall.getY() <= this.getCenterY()){
            end = System.currentTimeMillis();
            time = (end-start)/10F;
            distance = this.getCenterX()-RealFunction.target.getX();
            init();
            return true;
        }
        return false;
    }
    public boolean reachedGoal(Target target){
        if(target.getX()+target.getWidth() >= this.getCenterX() && target.getX() <= this.getCenterX()
                    && target.getY() <= this.getCenterY()){
            end = System.currentTimeMillis();
            time = (end-start)/10F;
            distance = this.getCenterX()-RealFunction.target.getX();
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

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
