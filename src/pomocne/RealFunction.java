package pomocne;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.stat.DoubleMomentStatistics;
import javafx.scene.paint.Color;
import main.Details;
import main.Target;
import main.Wall;

import java.util.ArrayList;

public class RealFunction {
    public static Target target;
    static EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();
    public static ArrayList<Details> individualProperties = new ArrayList<>();

    static int[] generationIndividuals = new int[100];

    // Definition of the fitness function.
    private static synchronized Double eval(final Genotype<DoubleGene> gt) {
        final double x = gt.getGene().doubleValue();
        final double y = gt.getChromosome(1).getGene().doubleValue();
        Missile2 missile = new Missile2(120,630, 20, Color.DARKRED);
        target = new Target(1050, 600, 100,100, Color.DARKGREEN);
        Wall wall = new Wall(600,300,30,400, Color.DARKGREY);
        missile.setVelocity(x);
        missile.setDegrees(y);
        boolean czy = true;
        int end = 0;
        while(czy) {
            missile.move();
            end++;
            if(missile.checkCollision(wall) || missile.reachedGoal(target)){
                czy = false;
            }
        }
        //System.out.println(statistics);
       // System.out.println(x+";"+y);
        generationIndividuals[(int)statistics.getInvalids().toIntMoments().getCount()]++;
        return Math.abs(missile.getDistance()) + end;
    }

    public static void main(String[] args) {
        // Create/configuring the engine via its builder.
        final Engine<DoubleGene, Double> engine = Engine
                .builder(
                        RealFunction::eval,
                        DoubleChromosome.of(1, 25),
                        DoubleChromosome.of(1, 90))
                .optimize(Optimize.MINIMUM)
                .populationSize(10)
                .alterers(
                        new SinglePointCrossover<>(1.0),
                        new SwapMutator<>(0.03))
                .build();

        // Execute the GA (engine).
        final Phenotype<DoubleGene, Double> result = engine.stream()
                // Truncate the evolution stream if no better individual could
                // be found after 5 consecutive generations.
                .limit(Limits.bySteadyFitness(3))
                .limit(100)
                .peek(statistics)
                /*.peek(r -> System.out.println("GENERACJA: " + r.getGeneration()+"\n" +
                        r.getPopulation().map(e-> e.getGenotype().getGene().doubleValue()) + "\n----\n"+
                        r.getPopulation().map(e -> + e.getGenotype().getChromosome(1).getGene().doubleValue())))*/
                .peek(r -> r.getPopulation().forEach(name ->
                        System.out.println(name.getGenotype().getGene().doubleValue() + " " +
                                name.getGenotype().getChromosome(1).getGene().doubleValue() + " fit: "
                        + name.getFitness().doubleValue())
                ))
                //.peek(r -> System.out.println(r.getGenotypes))
                .collect(EvolutionResult.toBestPhenotype());
        int suma = 0;
        for(int i=0;generationIndividuals[i]!=0;i++){
            System.out.println("Generation: " + i + " - " + generationIndividuals[i]);
            suma+=generationIndividuals[i];
        }
        System.out.println("suma: " + suma);
        System.out.println(result.getFitness());
        System.out.println(result.getGenotype().getChromosome(0).getGene().doubleValue());
        System.out.println(result.getGenotype().getChromosome(1).getGene().doubleValue());
    }
}