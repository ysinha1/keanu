package io.improbable.keanu.ABM;

import io.improbable.keanu.research.VertexBackedRandomFactory;
import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class testABM {

    @Test
    public void testRemoveAgent() {
        Simulation simulation = new Simulation(20, 20,
            new VertexBackedRandomFactory(100, 100),
            10,
            10,
            5,
            0.02,
            0.06,
            0.03);

        for (int i=0; i<simulation.grid.length; i++) {
            for (int j=0; j<simulation.grid.length; j++) {
                if (simulation.grid[i][j] != null) {
                    simulation.removeAgent(i, j);
                }
            }
        }
        Integer simulationCensusSystemTotal = simulation.numberOfPredators + simulation.numberOfPrey;

        Pair<Integer, Integer> manualPopulationCheck = manuallyCheckPopulation(simulation.grid);
        Integer manualPopulationCensus = manualPopulationCheck.getFirst() + manualPopulationCheck.getSecond();

        assertEquals(0, simulationCensusSystemTotal, 0);
        assertEquals(0, manualPopulationCensus, 0);
    }

    @Test
    public void testPredatorHungerAttrition() {
        Simulation simulation = new Simulation(5, 5,
            new VertexBackedRandomFactory(0, 1000),
            10,
            0,
            0,
            0.02,
            0.06,
            0.03);

        simulation.spawnPredator(2, 2);

        while (simulation.numberOfPredators > 0 && simulation.currentTime < 10000) {
            simulation.step();
            simulation.currentTime++;
        }
        System.out.println(simulation.currentTime);

        assertEquals(0, simulation.numberOfPredators, 0);
    }

    @Test
    public void testHunting() {
        Simulation simulation = new Simulation(3, 3,
            new VertexBackedRandomFactory(0, 1000),
            10,
            0,
            0,
            0.02,
            0.06,
            0.03);

        simulation.spawnPredator(0, 0);
        simulation.spawnPrey(1, 1);

        System.out.println("Test population spawned");
        System.out.println("Prey population: " + simulation.numberOfPrey);

        while (simulation.numberOfPrey > 0 && simulation.currentTime < 10000) {
            simulation.step();
            simulation.currentTime++;
        }
        System.out.println(simulation.currentTime);

        assertEquals(0, simulation.numberOfPrey, 0);
    }

    @Test
    public void testPreyReproductionAndPopulationControl() {
        Simulation simulation = new Simulation(3, 3,
            new VertexBackedRandomFactory(0, 1000),
            1000,
            0,
            0,
            0.02,
            0.06,
            0.03);

        simulation.spawnPrey(1, 1);
        simulation.run();

        assertEquals(6, simulation.numberOfPrey, 1);
    }


    @Test
    public void testLongTermPopulationAccounting() {
        Simulation simulation = new Simulation(20, 20,
            new VertexBackedRandomFactory(100, 1000),
            1000,
            20,
            5,
            0.02,
            0.06,
            0.03);

        while (simulation.numberOfPrey > 0 && simulation.currentTime < 10000) {
            simulation.step();
            Pair<Integer, Integer> pop = manuallyCheckPopulation(simulation.grid);
            assertEquals(pop.getFirst(), simulation.numberOfPrey, 0);
            assertEquals(pop.getSecond(), simulation.numberOfPredators, 0);
            simulation.currentTime++;
        }
    }

    public Pair<Integer, Integer> manuallyCheckPopulation(Agent[][] grid) {
        Integer noPrey = 0;
        Integer noPred = 0;
        for (Agent[] row : grid) {
            for (Agent agent : row) {
                if (agent != null) {
                    if (agent instanceof Predator) {
                        noPred += 1;
                    } else if (agent instanceof Prey) {
                        noPrey += 1;
                    }
                }
            }
        }
        return new Pair<>(noPrey, noPred);
    }


}

