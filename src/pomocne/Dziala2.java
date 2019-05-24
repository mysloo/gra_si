package pomocne;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Missile;
import main.Tower;
import main.Wall;
import main.Target;

import java.util.Scanner;


public class Dziala2 extends Application {

    public static final double APP_W = 1200, APP_H = 700;
    private Missile missile;
    public static final Timeline timeline = new Timeline();
    public static Target target;
    public Parent createGame() {
        Pane root = new Pane();
        root.setPrefSize(APP_W, APP_H);
        missile = new Missile(120,630, 20, Color.DARKRED);
        Tower tower = new Tower(100,650, 40, 50, Color.DARKBLUE);
        target = new Target(1050, 600, 100,100, Color.DARKGREEN);
        Wall wall = new Wall(600,300,30,400, Color.DARKGREY);

        KeyFrame frame = new KeyFrame(Duration.seconds(0.05), e ->{
            missile.move();
            missile.checkCollision(wall);
            missile.reachedGoal(target);
        });

        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(Timeline.INDEFINITE);

        root.getChildren().addAll(tower, missile, wall, target);
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(createGame());
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);

        missile = (Missile)scene.getRoot().getChildrenUnmodifiable().get(1);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e->{
            switch(e.getCode()){
                case UP:
                    System.out.println("koniec");
                    System.exit(123);
                    break;
            }
        });
        primaryStage.show();
        timeline.play();
    }



}
