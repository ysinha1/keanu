package io.improbable.keanu.ABM;

public class PredPreyPlotter {

    public static void plotPredPrey (Agent[][] grid, double currentTime) {
        int ySize = grid.length;
        int xSize = grid[0].length;
        double[][] plotData = new double[ySize][xSize];
        for (int row = 0; row < ySize; row++) {
            for (int col = 0; col < xSize; col++) {
                Agent agent = grid[row][col];
                if (agent instanceof Prey) {
                    plotData[row][col] = 2.0;
                } else if (agent instanceof Predator) {
                    plotData[row][col] = 1.0;
                } else {
                    plotData[row][col] = 0.0;
                }
            }
        }
        int datapointRenderSize = 20;

        new Basic2DPlotter(plotData, datapointRenderSize, true, 0, "Predator Prey Model @ t = " + currentTime);

    }
}
