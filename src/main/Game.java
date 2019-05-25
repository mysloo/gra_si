package main;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

public class Game extends Application {

    public static final double APP_W = 1200, APP_H = 700;
    public static Target target;
    public static Missile missile;
    public static ArrayList<Pair> stats;
    static int i = 0;
    // Definition of the fitness function.
    private static Double eval(final Genotype<DoubleGene> gt) {
        final double v = gt.getGene().doubleValue();
        final double d = gt.getChromosome(1).getGene().doubleValue();
        Missile missile = new Missile(120,630, 20, Color.DARKRED);
        Tower tower = new Tower(100,650, 40, 50, Color.DARKBLUE);
        target = new Target(1050, 600, 100,100, Color.DARKGREEN);
        Wall wall = new Wall(600,300,30,400, Color.DARKGREY);
        missile.setVelocity(v);
        missile.setDegrees(d);
        double end = 0.05;
        while(true) {
            missile.move();
            end++;
            if(missile.checkCollision(wall) || missile.reachedGoal(target)){
                missile.setCenterX(120);
                missile.setCenterY(630);
                break;
            }
        }
        stats.add(new Pair(v, d));
        //System.out.println(missile.getDistance() + " " + end);
        return Math.abs(missile.getDistance()) + end;
    }


    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle("Animation");
        Group root = new Group();
        Scene scene = new Scene(root, APP_W, APP_H, Color.WHITE);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        showAnimation(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        stats = new ArrayList<>();
        // Create/configuring the engine via its builder.
        final Engine<DoubleGene, Double> engine = Engine
                .builder(
                        Game::eval,
                        DoubleChromosome.of(1, 25),
                        DoubleChromosome.of(1, 90))
                .populationSize(500)
                .optimize(Optimize.MINIMUM)
                .alterers(
                        new Mutator<>(0.03),
                        new MeanAlterer<>(0.6))
                .build();

        // Execute the GA (engine).
        final Phenotype<DoubleGene, Double> result = engine.stream()
                // Truncate the evolution stream if no better individual could
                // be found after 5 consecutive generations.
                .limit(Limits.bySteadyFitness(  10))
                // Terminate the evolution after maximal 100 generations.
                .limit(100)
                .collect(EvolutionResult.toBestPhenotype());

        launch(args);
        System.out.println(result.getFitness());
        System.out.println(result.getGenotype().getChromosome(0).getGene().doubleValue());
        System.out.println(result.getGenotype().getChromosome(1).getGene().doubleValue());
    }

    private void showAnimation(final Scene scene) {
        missile = new Missile(120,630, 20, Color.DARKRED);
        Tower tower = new Tower(100,650, 40, 50, Color.DARKBLUE);
        target = new Target(1050, 600, 100,100, Color.DARKGREEN);
        Wall wall = new Wall(600,300,30,400, Color.DARKGREY);

        final Group root = (Group) scene.getRoot();
        root.getChildren().addAll(tower,missile,wall,target);
        missile.setVelocity(stats.get(i).getX());
        missile.setDegrees(stats.get(i).getY());
        Timeline tl = new Timeline();
        tl.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.seconds(0.0025), e ->{
            missile.move();
            if(missile.checkCollision(wall) || missile.reachedGoal(target)){
                missile.setCenterX(120);
                missile.setCenterY(630);
                try {
                    i++;
                    missile.setVelocity(stats.get(i).getX());
                    missile.setDegrees(stats.get(i).getY());
                }
                catch(IndexOutOfBoundsException es){
                    tl.stop();
                    Platform.exit();
                }
            }
        });

        tl.getKeyFrames().add(frame);
        tl.play();
    }
}

