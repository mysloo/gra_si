package main;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Game extends Application {

    public static final double APP_W = 1200, APP_H = 700;
    public static Target target;
    public static Wall wall;
    private static Missile missile;
    private static ArrayList<Details> individualDetails;
    private ArrayList<Double> plotData = new ArrayList<>();
    private static int i = 0, j = 1;
    private static int var = 0;
    private double pcross, pmutation, bestSolution, bestVelocity, bestDegree;
    private int popsize, iterations;
    private Label generationLabel, fitnessLabel, pcLabel, pmLabel, popsizeLabel, iterLabel, chooseGenLabel;
    private TextField pcField, pmField, popsizeField, iterField;
    private Button startButton, stopButton, breakButton, plotButton;
    private ComboBox<Integer> chooseGeneration;

    // Definition of the fitness function.
    private static synchronized Double evaluateFitness(final Genotype<DoubleGene> gt) {
        final double v = gt.getGene().doubleValue();
        final double d = gt.getChromosome(1).getGene().doubleValue();
        missile.setVelocity(v);
        missile.setDegrees(d);
        missile.setTime(0);
        while(true) {
            missile.move();
            if(missile.checkCollision(wall) || missile.reachedGoal(target)){
                missile.setUpDefaultSpot();
                break;
            }
        }
        return Math.abs(missile.getDistance()) + missile.getTime();
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage){
        primaryStage.setTitle("Animation");
        Group root = new Group();
        Scene scene = new Scene(root, APP_W, APP_H, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        initGame(scene);
        primaryStage.show();
    }

    private void initGame(final Scene scene){
        Timeline tl = new Timeline();
        tl.setCycleCount(Animation.INDEFINITE);
        missile = new Missile(120,630, 20, Color.DARKRED);
        Tower tower = new Tower(100,650, 40, 50, Color.DARKBLUE);
        target = new Target(1050, 600, 100,100, Color.DARKGREEN);
        wall = new Wall(600,300,30,400, Color.DARKGREY);

        final Group root = (Group) scene.getRoot();
        root.getChildren().addAll(tower, missile, wall, target);
        initControls(scene);

        breakButton.setOnMouseClicked(e->{
            i = 0;
            j = 1;
            tl.stop();
            missile.setUpDefaultSpot();
            generationLabel.setText("Generation: " + j);
            startButton.setDisable(false);
        });

        stopButton.setOnMouseClicked(e->{
            if(var == 0){
                tl.stop();
                var = 1;
            }
            else {
                tl.play();
                var = 0;
            }
        });

        startButton.setOnMouseClicked(e->{
            if(validInputs(pcField.getText(), pmField.getText(), popsizeField.getText(), iterField.getText())) {
                chooseGeneration.getItems().clear();
                plotData = new ArrayList<>();
                tl.stop();
                startButton.setDisable(true);
                stopButton.setDisable(false);
                i = 0;
                j = 1;
                missile.setUpDefaultSpot();
                generationLabel.setText("Generation: " + j);
                individualDetails = new ArrayList<>();

                executeGeneticsAlgorithm();

                try {
                    writeToFile(individualDetails);
                }catch(IOException ex){ }

                fillChooseGenerationComboBox();

                missile.setVelocity(individualDetails.get(0).getVelocity());
                missile.setDegrees(individualDetails.get(0).getDegrees());
                tl.play();
            }
        });

        plotButton.setOnMouseClicked(e->{
            NumberAxis xAxis = new NumberAxis(1, plotData.size(), 1);
            xAxis.setLabel("Generations");

            NumberAxis yAxis = new NumberAxis   (0, 500, 50);
            yAxis.setLabel("Fitness value");

            LineChart linechart = new LineChart(xAxis, yAxis);
            
            XYChart.Series series = new XYChart.Series();
            series.setName("Best");
            double best = Double.MAX_VALUE;
            for(int i = 0; i < plotData.size(); i++){
                best = Math.min(best, plotData.get(i));
                series.getData().add(new XYChart.Data(i+1, best));
            }

            linechart.getData().add(series);
            linechart.setPrefSize(APP_W, APP_H);

            Group plot = new Group();
            plot.getChildren().addAll(linechart);
            Stage stage = new Stage();
            stage.setTitle("My New Stage Title");
            stage.setScene(new Scene(plot));
            stage.setResizable(false);
            stage.show();
        });


        tl.getKeyFrames().add(showAnimation(tl));
    }

    public KeyFrame showAnimation(Timeline tl){
        KeyFrame frame = new KeyFrame(Duration.seconds(0.0015), e ->{
            missile.move();
            if(missile.checkCollision(wall) || missile.reachedGoal(target)){
                missile.setUpDefaultSpot();
                try {
                    fitnessLabel.setText("Fittnes value: " + String.format("%.2f", individualDetails.get(i).getFitness()));
                    i++;
                    missile.setVelocity(individualDetails.get(i).getVelocity());
                    missile.setDegrees(individualDetails.get(i).getDegrees());
                    if(i % popsize == 0 && i!=individualDetails.size()){
                        j++;
                        generationLabel.setText("Generation: " + j);
                    }
                }
                catch(IndexOutOfBoundsException es){
                    tl.stop();
                    showAlert("Found best solution: \nfitness value: " + String.format("%.2f",bestSolution) +
                            "\nbest velocity: " + String.format("%.2f", bestVelocity) +
                            "\nbest degree: " + String.format("%.2f", bestDegree), Alert.AlertType.INFORMATION);
                    startButton.setDisable(false);
                }
            }
        });
        return frame;
    }
    public void fillChooseGenerationComboBox(){
        int n = individualDetails.size()/popsize;
        for(int i = 1; i <= n; i ++){
            chooseGeneration.getItems().add(i);
        }
        chooseGeneration.setOnAction(es->{
            try {
                i = (chooseGeneration.getValue()-1)*popsize;
                j = chooseGeneration.getValue();
                missile.setUpDefaultSpot();
                missile.setVelocity(individualDetails.get(i).getVelocity());
                missile.setDegrees(individualDetails.get(i).getDegrees());
                generationLabel.setText("Generation: " + j);
            }
            catch(NullPointerException e1){
                generationLabel.setText("Generation: " + j);
                fitnessLabel.setText("Fitness value: Infinity");
            }
        });
    }
    public Engine<DoubleGene, Double> configureGeneticsAlgorithm(){
        // Create/configuring the engine via its builder.
        final Engine<DoubleGene, Double> engine = Engine
                .builder(
                        Game::evaluateFitness,
                        DoubleChromosome.of(1, 25),
                        DoubleChromosome.of(1, 90))
                .populationSize(popsize)
                .optimize(Optimize.MINIMUM)
                .alterers(
                        new SinglePointCrossover<>(pcross),
                        new Mutator<>(pmutation))
                .build();
        return engine;
    }
    public void executeGeneticsAlgorithm(){
        final Engine<DoubleGene, Double> engine = configureGeneticsAlgorithm();
        // Execute the GA (engine).
        final Phenotype<DoubleGene, Double> result = engine.stream()
                // Truncate the evolution stream if no better individual could
                // be found after 5 consecutive generations.
                .limit(Limits.bySteadyFitness(10))
                .limit(iterations)
                .peek(r -> r.getPopulation().forEach(gene ->
                        individualDetails.add(new Details(
                                gene.getGenotype().getGene().doubleValue(),
                                gene.getGenotype().getChromosome(1).getGene().doubleValue(),
                                gene.getFitness().doubleValue())
                        ))
                )
                .peek(r -> plotData.add(r.getBestFitness()))
                .collect(EvolutionResult.toBestPhenotype());

        System.out.println("\n" + result.getFitness());
        System.out.println("\t" + result.getGenotype().getChromosome(0).getGene().doubleValue());
        System.out.println("\t" + result.getGenotype().getChromosome(1).getGene().doubleValue());

        bestSolution = result.getFitness();
        bestVelocity = result.getGenotype().getChromosome(0).getGene().doubleValue();
        bestDegree = result.getGenotype().getChromosome(1).getGene().doubleValue();
    }

    public void writeToFile(ArrayList<Details> list) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter("/home/mateusz/ideaProjects/gra_si/logs.txt"));
        int i = 0, generation = 1;
        double bestV = Double.MAX_VALUE, bestD = Double.MAX_VALUE, bestF = Double.MAX_VALUE;
        for(Details d : list){
            if(i % popsize == 0){
                pw.println("\nGeneration: " + generation);
                pw.println("\t\t\t\t\t\tvelocity\t\t\tdegrees\t\t\t\t\tfitness");
                generation++;
            }
            if(bestF > d.getFitness()){
                bestV = d.getVelocity();
                bestD = d.getDegrees();
                bestF = d.getFitness();
            }
            pw.println("individual: " + ((i%popsize)+1) +"\t[" + d.getVelocity() + "], [" + d.getDegrees() + "] -> [" + d.getFitness() + "]");
            i++;
            if( i % popsize == 0){
                pw.println("Best:\t\t\t" + "[" + bestV +"], " + "[" + bestD + "] -> [" + bestF + "]");
                bestV = Double.MAX_VALUE;
                bestD = Double.MAX_VALUE;
                bestF = Double.MAX_VALUE;
            }
        }
        pw.close();

    }
    public void initControls(final Scene scene){
        generationLabel = new Label("Generation: 1");
        generationLabel.setLayoutX(545);
        generationLabel.setLayoutY(10);
        generationLabel.setFont(Font.font(null,FontWeight.BOLD, 22));

        fitnessLabel = new Label("Fitness value: Infinity");
        fitnessLabel.setLayoutX(545);
        fitnessLabel.setLayoutY(45);
        fitnessLabel.setFont(Font.font(null,FontWeight.BOLD, 16));

        pcLabel = new Label("Pcrossover: ");
        pcLabel.setLayoutY(10);
        pcField = new TextField("0.6");
        pcField.setPadding(new Insets(5,5,5,5));
        pcField.setLayoutX(85);
        pcField.setLayoutY(10);
        pcField.setPrefWidth(70);

        pmLabel = new Label("Pmutation: ");
        pmLabel.setLayoutY(40);
        pmField = new TextField("0.03");
        pmField.setPadding(new Insets(5,5,5,5));
        pmField.setLayoutX(85);
        pmField.setLayoutY(40);
        pmField.setPrefWidth(70);

        popsizeLabel = new Label("Popsize: ");
        popsizeLabel.setLayoutY(70);
        popsizeField = new TextField("10");
        popsizeField.setPadding(new Insets(5,5,5,5));
        popsizeField.setLayoutX(85);
        popsizeField.setLayoutY(70);
        popsizeField.setPrefWidth(70);

        iterLabel = new Label("Maxiter: ");
        iterLabel.setLayoutY(100);
        iterField = new TextField("100");
        iterField.setPadding(new Insets(5,5,5,5));
        iterField.setLayoutX(85);
        iterField.setLayoutY(100);
        iterField.setPrefWidth(70);

        startButton = new Button("Start");
        startButton.setLayoutX(10);
        startButton.setLayoutY(140);

        stopButton = new Button("Stop");
        stopButton.setLayoutX(80);
        stopButton.setLayoutY(140);
        stopButton.setDisable(true);

        chooseGenLabel = new Label("Pick a generation: ");
        chooseGenLabel.setLayoutX(180);
        chooseGenLabel.setLayoutY(10);
        chooseGeneration = new ComboBox<>();
        chooseGeneration.setLayoutX(310);
        chooseGeneration.setLayoutY(10);
        chooseGeneration.setPlaceholder(new Label("empty"));

        breakButton = new Button("Break");
        breakButton.setLayoutX(10);
        breakButton.setLayoutY(180);

        plotButton = new Button("Show plot");
        plotButton.setLayoutY(220);
        plotButton.setLayoutX(10);

        final Group root = (Group) scene.getRoot();
        root.getChildren().addAll(pcLabel, breakButton, stopButton, pmLabel, popsizeLabel,
                                iterLabel, chooseGenLabel, chooseGeneration, generationLabel,
                                fitnessLabel, pcField, pmField, popsizeField, iterField, startButton, plotButton);
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


    //im wieksza populacja tym optymalniejszy fitness w pierwszej genreacji
    //zle zapisuje anjlepszego osobnika w pliku (parami powinno byc szukane)
    //pokazywac te mutacje i krzyzowanie dla popsize = 10 pm = 0.4

}

