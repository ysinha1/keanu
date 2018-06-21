package io.improbable.keanu.ABM;

public class testPlotting {

    int xSize;
    int ySize;
    int datapointRenderSize;
    boolean axis;
    double[][] plotData;

    public testPlotting(double[][] dataIn,int datapointRenderSize,
                        boolean axis, int display){
        this.datapointRenderSize = datapointRenderSize;
        this.axis = axis;
        ySize = dataIn.length;
        xSize = dataIn[0].length;

        boolean logPlot = false;
        int displayType = display;

        if ((display > 5) || (display < 0)) {
        System.out.println("input error, terminating");
        System.exit(0);
        }
        switch (display) {
            case 3:
                displayType = 0;
                logPlot = true;
                break;
            case 4:
                displayType = 1;
                logPlot = true;
                break;
            case 5:
                displayType = 2;
                logPlot = true;
                break;
        }

        plotData = new double[ySize][xSize];
        for(int row=0; row<ySize; row++){
            for(int col=0; col<xSize; col++) {
                plotData[row][col] = dataIn[row][col];
                if (logPlot) {
                    if (plotData[row][col] < 0) {
                        plotData[row][col] = -plotData[row][col];
                    }
                    if (plotData[row][col] > 0) {
                        plotData[row][col] = Math.log10(plotData[row][col]);
                    }
                }
            }
        }
    }




    public static void main (String[] args) {

        Integer xSize = 30;
        Integer ySize = 30;
        double[][] testInput = new double[ySize][xSize];

        for(int row=0; row<ySize; row++){
            for(int col=0; col<xSize; col++){
                int xSquare = col * col;
                int ySquare = row * row;
                testInput[row][col] = xSquare + ySquare;
            }
        }
    }
}
