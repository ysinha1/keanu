package io.improbable.keanu.ABM;

import io.improbable.keanu.research.Array2D;
import io.improbable.keanu.research.TriFunction;
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
                      Agent[][] initialState,
                      Double preyReproductionGradient, Double preyReproductionConstant,
                      Double predReproductionGradient) {
        assert XSIZE >= 3 || YSIZE >= 3: "Domain size must be 3x3 or greater";
        this.timesteps = timesteps;
        this.preyReproductionGradient = preyReproductionGradient;
        this.preyReproductionConstant = preyReproductionConstant;
        this.predReproductionGradient = predReproductionGradient;
        this.random = random;
        output.initialiseJSON(XSIZE, YSIZE, preyReproductionGradient, preyReproductionConstant, predReproductionGradient);


    }

    public Simulation(int XSIZE, int YSIZE, VertexBackedRandomFactory random, Integer timesteps,
                      Integer initialNumberOfPrey, Integer initialNumberOfPredators,
                      Double preyReproductionGradient, Double preyReproductionConstant,
                      Double predReproductionGradient) {
//        Agent[][] startGrid = new Agent[XSIZE][YSIZE];

        Agent[][] startGrid = initialiseSimulation(startGrid, initialNumberOfPrey, initialNumberOfPredators);
        Simulation(XSIZE, YSIZE, random, startGrid, preyReproductionGradient, preyReproductionConstant, predReproductionGradient);
    }

    public Simulation(Array2D<AbstractModel.Agents> grid, Double preyReproductionGradient, Double preyReproductionConstant,
                      Double predReproductionGradient) {
        int XSIZE = grid.iSize();
        int YSIZE = grid.jSize();
        int timesteps = 1;
        Agent[][] startGrid = new Agent[XSIZE][YSIZE];
        for (int i=0; i<XSIZE; i++) {
            for (int j=0; j<YSIZE; j++) {
                if (grid.get(i, j) == AbstractModel.Agents.PREY) {
                    startGrid = spawnPrey(startGrid, i, j);
                } else if (grid.get(i, j) == AbstractModel.Agents.PREDATOR) {
                    startGrid = spawnPredator(startGrid, i, j);
                }
            }
        }
        // TODO complete this constructor
//        Simulation(XSIZE, YSIZE, ...)

    }


    private Agent[][] initialiseSimulation(Agent[][] startGrid, int numberOfPrey, int numberOfPredators) {
        startGrid = randomSpawnPopulation(startGrid, numberOfPredators, this::spawnPredator);
        startGrid = randomSpawnPopulation(startGrid, numberOfPrey, this::spawnPrey);
        System.out.println("Simulation initialised");
        return startGrid;
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

    private Agent[][] randomSpawnPopulation(Agent[][] startGrid, Integer numberToSpawn, TriFunction<Agent[][], Integer, Integer, Agent[][]> function) {
        int i = 0;
        while (i < numberToSpawn) {
            int proposedX = random.nextDouble(0, startGrid.length).intValue();
            int proposedY = random.nextDouble(0, startGrid[0].length).intValue();
            if (getXY(proposedX, proposedY) == null) {
                startGrid = function.apply(startGrid, proposedX, proposedY);
                i++;
            }
        }
        return startGrid;
    }

    public Agent getXY(int xLocation, int yLocation) {
        return grid[(xLocation+grid.length)%grid.length][(yLocation+grid[0].length)%grid[0].length];
    }

    public Agent[][] spawnPrey(Agent[][] startGrid, int startX, int startY) {
        startGrid[startX][startY] = new Prey(this, startX, startY, preyReproductionGradient, preyReproductionConstant);
        numberOfPrey += 1;
        System.out.println("- Prey spawned at: " + startX + ", " + startY + "\t\t Total count: " + numberOfPrey);
        return startGrid;
    }

    public Agent[][] spawnPredator(Agent[][] startGrid, int startX, int startY) {
        startGrid[startX][startY] = new Predator(this, startX, startY, predReproductionGradient);
        numberOfPredators += 1;
        System.out.println("- Predator spawned at: " + startX + ", " + startY + "\t\t Total count: " + numberOfPredators);
        return startGrid;
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
