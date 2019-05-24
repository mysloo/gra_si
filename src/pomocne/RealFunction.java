package pomocne;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;

public class RealFunction {
    // Definition of the fitness function.
    private static Double eval(final Genotype<DoubleGene> gt) {//a w tej funkcji robic animacje,
        // podejmowac decyzje przez siec i obliczac fitness
        final double x = gt.getGene().doubleValue();//odleglosc
        final double y = gt.getChromosome(1).getGene().doubleValue();//czas
        //return Math.cos(0.5 + Math.sin(x))*Math.cos(x);
        return Math.abs(x) + y;
    }

    public static void main(String[] args) {
        // Create/configuring the engine via its builder.
        final Engine<DoubleGene, Double> engine = Engine
                .builder(
                        RealFunction::eval,
                        //DoubleChromosome.of(0.0, 2.0*Math.PI))
                        DoubleChromosome.of(-5, 5),//to chyba powinna byc przekazywana kazda z wag
                        DoubleChromosome.of(-10, 10))
                .populationSize(500)
                .optimize(Optimize.MAXIMUM)
                .alterers(
                        new Mutator<>(0.03),
                        new MeanAlterer<>(0.6))
                .build();

        // Execute the GA (engine).
        final Phenotype<DoubleGene, Double> result = engine.stream()
                // Truncate the evolution stream if no better individual could
                // be found after 5 consecutive generations.
                .limit(Limits.bySteadyFitness(5))
                // Terminate the evolution after maximal 100 generations.
                .limit(100)
                .collect(EvolutionResult.toBestPhenotype());
        System.out.println(result.getFitness());
        System.out.println(result.getGenotype().getChromosome(0).getGene().doubleValue());
        System.out.println(result.getGenotype().getChromosome(1).getGene().doubleValue());
    }
}

