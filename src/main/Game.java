package main;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.stat.DoubleMomentStatistics;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

public class Game extends Application {

    public static final double APP_W = 1200, APP_H = 700;
    public static Target target;
    public static Missile missile;
    public static ArrayList<Pair> individualProperties;
    static EvolutionStatistics<Double, DoubleMomentStatistics> statistics;
    static int[] generationIndividuals;
    static ArrayList<Double> fitnessValue;
    static int i = 0;
    static int ile = 0;
    static int prefixSum = -1;
    static int j = 0;
    double pcross, pmutation;
    int popsize, iterations;
    // Definition of the fitness function.
    private static synchronized Double eval(final Genotype<DoubleGene> gt) {
        final double v = gt.getGene().doubleValue();
        final double d = gt.getChromosome(1).getGene().doubleValue();
        Missile missile = new Missile(120,630, 20, Color.DARKRED);
        Tower tower = new Tower(100,650, 40, 50, Color.DARKBLUE);
        target = new Target(1050, 600, 100,100, Color.DARKGREEN);
        Wall wall = new Wall(600,300,30,400, Color.DARKGREY);
        missile.setVelocity(v);
        missile.setDegrees(d);
        double end = 0.25;
        ile++;
        while(true) {
            missile.move();
            end+=0.25;
            if(missile.checkCollision(wall) || missile.reachedGoal(target)){
                missile.setCenterX(120);
                missile.setCenterY(630);
                break;
            }
        }
        individualProperties.add(new Pair(v, d));
        generationIndividuals[(int)statistics.getInvalids().toIntMoments().getCount()]++;
        //fitnessValue.add(statistics.getFitness().toDoubleMoments().getMin());
        fitnessValue.add(Math.abs(missile.getDistance()) + end);
        return Math.abs(missile.getDistance()) + end;
    }


    public static void main(String[] args) {
        launch(args);
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

    private void showAnimation(final Scene scene) {
        Timeline tl = new Timeline();
        missile = new Missile(120,630, 20, Color.DARKRED);
        Tower tower = new Tower(100,650, 40, 50, Color.DARKBLUE);
        target = new Target(1050, 600, 100,100, Color.DARKGREEN);
        Wall wall = new Wall(600,300,30,400, Color.DARKGREY);

        Label generationLabel = new Label();
        generationLabel.setLayoutX(545);
        generationLabel.setLayoutY(10);
        generationLabel.setFont(Font.font(null,FontWeight.BOLD, 22));
        Label fitnessLabel = new Label();
        fitnessLabel.setLayoutX(545);
        fitnessLabel.setLayoutY(45);
        fitnessLabel.setFont(Font.font(null,FontWeight.BOLD, 16));
        TextField pcField = new TextField("P. krzyzowania");
        pcField.setPadding(new Insets(5,5,5,5));
        TextField pmField = new TextField("P. mutacji");
        pmField.setPadding(new Insets(5,5,5,5));
        pmField.setLayoutY(30);
        TextField popsizeField = new TextField("Rozmiar populacji");
        popsizeField.setPadding(new Insets(5,5,5,5));
        popsizeField.setLayoutY(60);
        TextField iterField = new TextField("Liczba generacji");
        iterField.setPadding(new Insets(5,5,5,5));
        iterField.setLayoutY(90);
        Button save = new Button("Zapisz");
        save.setLayoutY(120);
        final Group root = (Group) scene.getRoot();
        root.getChildren().addAll(tower,missile,wall,target, generationLabel, fitnessLabel, pcField,pmField,popsizeField,iterField, save);

        save.setOnMouseClicked(e->{
            if(validInputs(pcField.getText(), pmField.getText(), popsizeField.getText(), iterField.getText())) {
                tl.stop();
                statistics = EvolutionStatistics.ofNumber();
                individualProperties = new ArrayList<>();
                generationIndividuals = new int[iterations];
                fitnessValue = new ArrayList<>();
                // Create/configuring the engine via its builder.
                final Engine<DoubleGene, Double> engine = Engine
                        .builder(
                                Game::eval,
                                DoubleChromosome.of(1, 25),
                                DoubleChromosome.of(1, 90))
                        .populationSize(popsize)
                        .optimize(Optimize.MINIMUM)
                        .alterers(
                                new Mutator<>(pmutation),
                                new MeanAlterer<>(pcross))
                        .build();

                // Execute the GA (engine).
                final Phenotype<DoubleGene, Double> result = engine.stream()
                        // Truncate the evolution stream if no better individual could
                        // be found after 5 consecutive generations.
                        .limit(Limits.bySteadyFitness(10))
                        .limit(iterations)
                        .peek(statistics)
                        .collect(EvolutionResult.toBestPhenotype());
                System.out.println(result.getFitness());
                System.out.println(result.getGenotype().getChromosome(0).getGene().doubleValue());
                System.out.println(result.getGenotype().getChromosome(1).getGene().doubleValue());
                missile.setVelocity(individualProperties.get(i).getX());
                missile.setDegrees(individualProperties.get(i).getY());
                i = ile*95/100;
                tl.play();
            }
        });



        tl.setCycleCount(Animation.INDEFINITE);
        KeyFrame frame = new KeyFrame(Duration.seconds(0.0025), e ->{
            missile.move();
            if(missile.checkCollision(wall) || missile.reachedGoal(target)){
                missile.setCenterX(120);
                missile.setCenterY(630);
                try {
                    if(i > prefixSum){
                        prefixSum += generationIndividuals[j];
                        generationLabel.setText("Generation: " + j);
                        j++;
                    }
                    fitnessLabel.setText("Fittnes value: " + String.format("%.2f", fitnessValue.get(i)));
                    i++;
                    missile.setVelocity(individualProperties.get(i).getX());
                    missile.setDegrees(individualProperties.get(i).getY());
                }
                catch(IndexOutOfBoundsException es){
                    tl.stop();
                    Platform.exit();
                }
            }
        });
        tl.getKeyFrames().add(frame);
    }

    public boolean validInputs(String pc, String pm, String pop, String iter){
        try {
            pcross = Double.parseDouble(pc);
        }
        catch(NumberFormatException exc ){
            showAlert("Pcross - wrong format");
            return false;
        }
        try {
            pmutation = Double.parseDouble(pm);
        }
        catch(NumberFormatException exc ){
            showAlert("Pmutation - wrong format");
            return false;
        }
        try {
            popsize = Integer.parseInt(pop);
        }
        catch(NumberFormatException exc ){
            showAlert("Popsize - wrong format");
            return false;
        }
        try {
            iterations = Integer.parseInt(iter);
        }
        catch(NumberFormatException exc ){
            showAlert("Iterations - wrong format");
            return false;
        }
        finally{
            if(pcross < 0 || pmutation < 0 || popsize < 0 || iterations < 0){
                showAlert("Values cannot be negative");
                return false;
            }
        }
        return true;
    }
    public void showAlert(String msg){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }


}

