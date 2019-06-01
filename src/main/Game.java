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
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

public class Game extends Application {

    public static final double APP_W = 1200, APP_H = 700;
    public static Target target;
    public Missile missile;
    public static ArrayList<Pair> individualProperties;
    static EvolutionStatistics<Double, DoubleMomentStatistics> statistics;
    static int[] generationIndividuals;
    static ArrayList<Double> fitnessValue;
    static int i = 0;
    static int ile = 0;
    static int prefixSum[];
    static int j = 0;
    double pcross, pmutation;
    int popsize, iterations;
    double bestSolution, bestVelocity, bestDegree;
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

        Label generationLabel = new Label("Generation: 0");
        generationLabel.setLayoutX(545);
        generationLabel.setLayoutY(10);
        generationLabel.setFont(Font.font(null,FontWeight.BOLD, 22));
        Label fitnessLabel = new Label("Fitness value: Infinity");
        fitnessLabel.setLayoutX(545);
        fitnessLabel.setLayoutY(45);
        fitnessLabel.setFont(Font.font(null,FontWeight.BOLD, 16));
        Label pcLabel = new Label("Pcrossover: ");
        pcLabel.setLayoutY(10);
        TextField pcField = new TextField();
        pcField.setPadding(new Insets(5,5,5,5));
        pcField.setLayoutX(85);
        pcField.setLayoutY(10);
        pcField.setPrefWidth(70);
        Label pmLabel = new Label("Pmutation: ");
        pmLabel.setLayoutY(40);
        TextField pmField = new TextField();
        pmField.setPadding(new Insets(5,5,5,5));
        pmField.setLayoutX(85);
        pmField.setLayoutY(40);
        pmField.setPrefWidth(70);
        Label popsizeLabel = new Label("Popsize: ");
        popsizeLabel.setLayoutY(70);
        TextField popsizeField = new TextField();
        popsizeField.setPadding(new Insets(5,5,5,5));
        popsizeField.setLayoutX(85);
        popsizeField.setLayoutY(70);
        popsizeField.setPrefWidth(70);
        Label iterLabel = new Label("Maxiter: ");
        iterLabel.setLayoutY(100);
        TextField iterField = new TextField();
        iterField.setPadding(new Insets(5,5,5,5));
        iterField.setLayoutX(85);
        iterField.setLayoutY(100);
        iterField.setPrefWidth(70);
        Button saveButton = new Button("Execute");
        saveButton.setLayoutX(10);
        saveButton.setLayoutY(130);
        Label chooseGenLabel = new Label("Peek a generation: ");
        chooseGenLabel.setLayoutX(180);
        chooseGenLabel.setLayoutY(10);
        ComboBox<Integer> chooseGeneration = new ComboBox<>();
        chooseGeneration.setLayoutX(310);
        chooseGeneration.setLayoutY(10);
        chooseGeneration.setPlaceholder(new Label("empty"));

        final Group root = (Group) scene.getRoot();
        root.getChildren().addAll(tower,missile,wall,target, pcLabel, pmLabel, popsizeLabel, iterLabel, chooseGenLabel,chooseGeneration, generationLabel, fitnessLabel, pcField,pmField,popsizeField,iterField, saveButton);

        saveButton.setOnMouseClicked(e->{
            if(validInputs(pcField.getText(), pmField.getText(), popsizeField.getText(), iterField.getText())) {
                tl.stop();
                saveButton.setDisable(true);
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
                bestSolution = result.getFitness();
                bestVelocity = result.getGenotype().getChromosome(0).getGene().doubleValue();
                bestDegree = result.getGenotype().getChromosome(1).getGene().doubleValue();
                missile.setVelocity(individualProperties.get(i).getX());
                missile.setDegrees(individualProperties.get(i).getY());
                i++;

                int n;
                for(n=0;generationIndividuals[n]!=0;n++);
                prefixSum = new int[n+1];
                for(int i = 1; i <= n; i++){
                    prefixSum[i] += generationIndividuals[i-1] + prefixSum[i-1];
                }
                for(int i = 0; i < n; i ++){
                    chooseGeneration.getItems().add(i);
                }
                chooseGeneration.setOnAction(es->{
                    tl.stop();
                    try {
                        i = prefixSum[chooseGeneration.getValue()];
                        j = chooseGeneration.getValue();
                        generationLabel.setText("Generation: " + j);
                    }
                    catch(NullPointerException e1){
                        j = 0;
                        i = 0;
                        generationLabel.setText("Generation: " + j);
                        fitnessLabel.setText("Fitness value: Infinity");
                    }
                    tl.play();
                });
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
                    if(i > prefixSum[j]){
                        generationLabel.setText("Generation: " + j);
                        j++;
                    }
                    fitnessLabel.setText("Fittnes value: " + String.format("%.2f", fitnessValue.get(i)));
                    i++;
                    missile.setVelocity(individualProperties.get(i).getX());
                    missile.setDegrees(individualProperties.get(i).getY());
                }
                catch(IndexOutOfBoundsException es){
                    chooseGeneration.getItems().clear();
                    tl.stop();
                    showAlert("Found best solution: \nfitness value: " + String.format("%.2f",bestSolution) +
                            "\nbest velocity: " + String.format("%.2f", bestVelocity) +
                            "\nbest degree: " + String.format("%.2f", bestDegree), Alert.AlertType.INFORMATION);
                    saveButton.setDisable(false);
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
            showAlert("Pcross - wrong format", Alert.AlertType.ERROR);
            return false;
        }
        try {
            pmutation = Double.parseDouble(pm);
        }
        catch(NumberFormatException exc ){
            showAlert("Pmutation - wrong format", Alert.AlertType.ERROR);
            return false;
        }
        try {
            popsize = Integer.parseInt(pop);
        }
        catch(NumberFormatException exc ){
            showAlert("Popsize - wrong format", Alert.AlertType.ERROR);
            return false;
        }
        try {
            iterations = Integer.parseInt(iter);
        }
        catch(NumberFormatException exc ){
            showAlert("Iterations - wrong format", Alert.AlertType.ERROR);
            return false;
        }
        finally{
            if(pcross < 0 || pmutation < 0 || popsize < 0 || iterations < 0){
                showAlert("Values cannot be negative", Alert.AlertType.ERROR);
                return false;
            }
            if(pcross > 1 || pmutation > 1){
                showAlert("Pcross and pmutation should be in a range [0,1]", Alert.AlertType.ERROR);
                return false;
            }
        }
        return true;
    }
    public void showAlert(String msg, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setContentText(msg);
        alert.setHeaderText(null);
        ((Stage)(alert.getDialogPane().getScene().getWindow())).setAlwaysOnTop(true);
        alert.show();
    }


}

