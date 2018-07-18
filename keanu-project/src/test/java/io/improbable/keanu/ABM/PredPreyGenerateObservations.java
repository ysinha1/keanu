package io.improbable.keanu.ABM;

import io.improbable.keanu.research.VertexBackedRandomFactory;

public class PredPreyGenerateObservations {

    public static void main (String[] args) {
        Simulation simulation = new Simulation(20, 20,
            new VertexBackedRandomFactory(100, 1000),
            1000,
            10,
            5,
            0.02,
            0.06,
            0.03);

        simulation.dumpFrequency = 75;
        simulation.run();

        System.out.println("Final number of Prey: " + simulation.numberOfPrey);
        System.out.println("Final number of Pred: " + simulation.numberOfPredators);
    }
}
