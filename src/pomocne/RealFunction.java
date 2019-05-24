package pomocne;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import javafx.scene.paint.Color;
import main.Missile;
import main.Target;
import main.Tower;
import main.Wall;

public class RealFunction {
    public static Target target;
    // Definition of the fitness function.
    private static Double eval(final Genotype<DoubleGene> gt) {
        final double x = gt.getGene().doubleValue();
        final double y = gt.getChromosome(1).getGene().doubleValue();
        Missile missile = new Missile(120,630, 20, Color.DARKRED);
        Tower tower = new Tower(100,650, 40, 50, Color.DARKBLUE);
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

        //System.out.println(missile.getDistance() + " " + missile.getTime());
        //return Math.abs(missile.getDistance()) + missile.getTime();
        System.out.println(missile.getDistance() + " " + end);
        return Math.abs(missile.getDistance()) + end;
    }

    public static void main(String[] args) {
        // Create/configuring the engine via its builder.
        final Engine<DoubleGene, Double> engine = Engine
                .builder(
                        RealFunction::eval,
                        //DoubleChromosome.of(0.0, 2.0*Math.PI))
                        DoubleChromosome.of(1, 25),
                        DoubleChromosome.of(1, 80))
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
        System.out.println(result.getFitness());
        System.out.println(result.getGenotype().getChromosome(0).getGene().doubleValue());
        System.out.println(result.getGenotype().getChromosome(1).getGene().doubleValue());
    }
}

