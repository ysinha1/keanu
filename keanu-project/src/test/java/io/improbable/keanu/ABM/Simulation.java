package io.improbable.keanu.ABM;

import io.improbable.keanu.research.VertexBackedRandomFactory;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class Simulation {

    Agent[][] grid;
    VertexBackedRandomFactory random;

    private Integer timesteps;
    public Integer currentTime = 0;
    Integer numberOfPredators = 0;
    Integer numberOfPrey = 0;
    private Double preyReproductionGradient;
    private Double preyReproductionConstant;
    private Double predReproductionGradient;
    private OutputWriter output = new OutputWriter();
    private ArrayList<Agent> agentsKilledDuringStep = new ArrayList<>();
    public Integer dumpFrequency = 1;

    public Simulation(int XSIZE, int YSIZE, VertexBackedRandomFactory random, Integer timesteps,
                      Integer initialNumberOfPrey, Integer initialNumberOfPredators,
                      Double preyReproductionGradient, Double preyReproductionConstant,
                      Double predReproductionGradient) {
        assert XSIZE >= 3 || YSIZE >= 3: "Domain size must be 3x3 or greater";
        this.timesteps = timesteps;
        this.preyReproductionGradient = preyReproductionGradient;
        this.preyReproductionConstant = preyReproductionConstant;
        this.predReproductionGradient = predReproductionGradient;

        grid = new Agent[XSIZE][YSIZE];
        this.random = random;

        output.initialiseJSON(XSIZE, YSIZE, preyReproductionGradient, preyReproductionConstant, predReproductionGradient);
        initialiseSimulation(initialNumberOfPrey, initialNumberOfPredators);
    }

    public Simulation(Pair<Integer, Integer> quadrantDimensions, Pair<Integer, Integer> quadrantArrangement, Agent[][] grid, Double preyReproductionGradient, Double preyReproductionConstant,
                      Double predReproductionGradient) {
        // Todo - to interface with abstract model, needs a constructor which allows a (initialised?) state to be fed in
    }


    private void initialiseSimulation(int numberOfPrey, int numberOfPredators) {
        randomSpawnPopulation(numberOfPredators, this::spawnPredator);
        randomSpawnPopulation(numberOfPrey, this::spawnPrey);
        System.out.println("Simulation initialised");
    }

    public void step() {
        ArrayList<Agent> agentsToStep = new ArrayList<>();
        for (Agent[] subset: grid) {
            for (Agent agent: subset) {
                if (agent != null) {
                    agentsToStep.add(agent);
                }
            }
        }
        for (Agent agent: agentsToStep) {
            if (!agentsKilledDuringStep.contains(agent)) {
                agent.step();
            }
        }
    }

    private void housekeeping() {
        System.out.println("Time: " + currentTime + "\t Predator population: " + numberOfPredators + "\t Prey population: " + numberOfPrey);
        output.dumpToJSON(currentTime.doubleValue(), numberOfPrey, numberOfPredators, grid);
        if (currentTime%dumpFrequency == 0) {PredPreyPlotter.plotPredPrey(grid, currentTime.doubleValue()); }
        agentsKilledDuringStep = new ArrayList<>();
    }

    public void run() {
        while (currentTime < timesteps) {
            step();
            housekeeping();
            currentTime++;
        }
    }

    private void randomSpawnPopulation(Integer numberToSpawn, BiConsumer<Integer, Integer> function) {
        int i = 0;
        while (i < numberToSpawn) {
            int proposedX = random.nextDouble(0, grid.length).intValue();
            int proposedY = random.nextDouble(0, grid[0].length).intValue();
            if (getXY(proposedX, proposedY) == null) {
                function.accept(proposedX, proposedY);
                i++;
            }
        }
    }

    public Agent getXY(int xLocation, int yLocation) {
        return grid[(xLocation+grid.length)%grid.length][(yLocation+grid[0].length)%grid[0].length];
    }

    public void spawnPrey(int startX, int startY) {
        grid[startX][startY] = new Prey(this, startX, startY, preyReproductionGradient, preyReproductionConstant);
        numberOfPrey += 1;
        System.out.println("- Prey spawned at: " + startX + ", " + startY + "\t\t Total count: " + numberOfPrey);
    }

    public void spawnPredator(int startX, int startY) {
        grid[startX][startY] = new Predator(this, startX, startY, predReproductionGradient);
        numberOfPredators += 1;
        System.out.println("- Predator spawned at: " + startX + ", " + startY + "\t\t Total count: " + numberOfPredators);
    }

    public void removeAgent(Integer xLocation, Integer yLocation) {
        if (grid[xLocation][yLocation] instanceof Prey) {
            numberOfPrey -= 1;
        } else if (grid[xLocation][yLocation] instanceof Predator) {
            numberOfPredators -= 1;
        }
        agentsKilledDuringStep.add(grid[xLocation][yLocation]);
        grid[xLocation][yLocation] = null;
    }
}
