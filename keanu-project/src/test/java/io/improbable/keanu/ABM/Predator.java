package io.improbable.keanu.ABM;

public class Predator extends Agent {

    private final Double predReproductionGradient; // 0.03

    private boolean hasEaten = false;

    Predator(Simulation sim, int startX, int startY, Double predReproductionGradient) {
        super(sim, startX, startY);
        this.predReproductionGradient = predReproductionGradient;
    }

    public void step() {
        hasEaten = false;
        super.step();
        hunt();
        controlPopulation();
    }

    private void hunt() {
        for (Agent agent: proximateAgents) {
            if (agent instanceof Prey) {
                sim.removeAgent(agent.xLocation, agent.yLocation);
                hasEaten = true;
            }
        }
    }

    private void controlPopulation() {
        if (!hasEaten && random.nextDouble(0, 240) < 1) {
            sim.removeAgent(xLocation, yLocation);
        } else if (hasEaten && random.nextDouble(0, 1) < (predReproductionGradient * getNumberOfProximatePredators())) {
            giveBirth(sim::spawnPredator);
        }
    }
}
