package io.improbable.keanu.ABM;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Basic2DPlotter extends Frame {

    private int xSize;
    private int ySize;
    private int datapointRenderSize;
    private boolean axis;
    private double[][] plotData;

    private int horizCenter;
    private int vertCenter;

    public Basic2DPlotter (double[][] dataIn,int datapointRenderSize, boolean axis, int display, String title) {
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
        for (int row = 0; row < ySize; row++) {
            for (int col = 0; col < xSize; col++) {
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

        normalisePlotData();

        Canvas surface = null;
        Canvas scale = null;

        switch (displayType) {
            case 0:
                surface = new GreyscalePlotCanvas();
                scale = new GreyscalePlotScale();
                break;
            case 1:
                surface = new ColourshiftPlotCanvas();
                scale = new ColourshiftPlotScale();
                break;
            case 2:
                surface = new ColourContourCanvas();
                scale = new ColourContourScale();
                break;
        }

        add(BorderLayout.CENTER, surface);
        add(BorderLayout.SOUTH, scale);
        pack();

        setTitle(title);
        setVisible(true);

        addWindowListener(new WindowAdapter(){ public void windowClosing(WindowEvent e){ System.exit(0); }});

    }

    public class GreyscalePlotCanvas extends Canvas {

        GreyscalePlotCanvas() {
            setSize(xSize*datapointRenderSize, ySize*datapointRenderSize);
            getCentre();
        }

        public void paint(Graphics g) {
            for (int row=0; row<ySize; row++) {
                for (int col=0; col<xSize; col++) {
                    convertToGreyScale((int) plotData[row][col], row, col, g);
                }
            }
            if (axis) { drawAxes(g); }
        }
    }

    public class GreyscalePlotScale extends Canvas {
        int scaleHeight = 6 * datapointRenderSize;

        GreyscalePlotScale () {
            setSize(xSize*datapointRenderSize, scaleHeight);
        }

        public void paint(Graphics g) {
            for (int row = 1; row < scaleHeight; row++) {
                for (int col = 0; col < xSize; col++) {
                    convertToGreyScale( 255 * col / (xSize - 1), row, col, g);
                }
            }
        }
    }

    public class ColourshiftPlotCanvas extends Canvas {

        ColourshiftPlotCanvas() {
            setSize(xSize * datapointRenderSize, ySize * datapointRenderSize);
            getCentre();
        }

        public void paint(Graphics g) {
            for (int row = 0; row < ySize; row++) {
                for (int col = 0; col < xSize; col++) {
                    convertToColour((int) plotData[row][col], row, col, g);
                }
            }
            if (axis) { drawAxes(g); }
        }
    }

    public class ColourshiftPlotScale extends Canvas {
        int scaleHeight = 6 * datapointRenderSize;

        ColourshiftPlotScale() {
            setSize(xSize * datapointRenderSize, scaleHeight);
        }

        public void paint(Graphics g) {
            for (int row = 1; row < scaleHeight; row++) {
                for (int col = 0; col < xSize; col++) {
                    convertToColour(255 * col / (xSize - 1), row, col, g);
                }
            }
        }
    }

    public class ColourContourCanvas extends Canvas {

        ColourContourCanvas() {
            setSize(xSize * datapointRenderSize, ySize * datapointRenderSize);
            getCentre();
        }

        public void paint(Graphics g) {
            Color[] colorPalette = getColourPalette();
            for (int row=0; row<ySize; row++) {
                for (int col=0; col<xSize; col++) {
                    int quantizedData = (int) (Math.round(plotData[row][col] * (colorPalette.length - 1) / 255));
                    g.setColor(colorPalette[quantizedData]);
                    g.fillRect(col*datapointRenderSize, row*datapointRenderSize, datapointRenderSize, datapointRenderSize);
                }
            }
            if (axis) { drawAxes(g); }
        }
    }

    public class ColourContourScale extends Canvas {
        int scaleHeight = 6 * datapointRenderSize;

        ColourContourScale() {
            setSize(xSize*datapointRenderSize, scaleHeight);
        }

        public void paint(Graphics g) {
            Color[] colorPalette = getColourPalette();
            for (int row=1; row<scaleHeight; row++) {
                for (int col=0; col<xSize; col++) {
                    double scaleValue = 255.0 * col / xSize;
                    int quantizedData = (int) (Math.round(scaleValue * (colorPalette.length - 1) / 255));
                    g.setColor(colorPalette[quantizedData]);
                    g.fillRect(col*datapointRenderSize, row*datapointRenderSize, datapointRenderSize, datapointRenderSize);
                }
            }
        }
    }

    private void drawAxes(Graphics g) {
        g.setColor(Color.RED);
        g.drawLine(0, vertCenter, 2 * horizCenter, vertCenter);
        g.drawLine(horizCenter, 0, horizCenter, 2 * vertCenter);
    }

    private void convertToColour(int value, int row, int col, Graphics g) {
        int red = 0;
        int green = 0;
        int blue = 0;
        if (value == 255) {
            red = green = blue = 255;
        } else if ((value > 0) && (value <= 63)) {
            int temp = 4 * (value);
            blue = 255;
            green = temp;
        } else if ((value > 63) && (value <= 127)) {
            int temp = 4 * (value - 64);
            green = 255;
            blue = 255 - temp;
        } else if ((value > 127) && (value <= 191)) {
            int temp = 4 * (value - 128);
            green = 255;
            red = temp;
        } else if ((value > 191) && (value <= 254)) {
            int temp = 4 * (value - 192);
            red = 255;
            green = 255 - temp;
        }
        Color colour = new Color(red, green, blue);
        g.setColor(colour);
        g.fillRect(col * datapointRenderSize, row * datapointRenderSize, datapointRenderSize, datapointRenderSize);
    }

    private void convertToGreyScale(int value, int row, int col, Graphics g) {
        Color colour = new Color(value, value, value);
        g.setColor(colour);
        g.fillRect(col * datapointRenderSize, row * datapointRenderSize, datapointRenderSize, datapointRenderSize);
    }

    private Color[] getColourPalette() {
        return new Color[] {
            Color.BLACK,
            Color.GRAY,
            Color.LIGHT_GRAY,
            Color.BLUE,
            new Color(100, 100, 255),
            new Color(140, 140, 255),
            new Color(175, 175, 255),
            Color.CYAN,
            new Color(140, 255, 255),
            Color.GREEN,
            new Color(140, 255, 140),
            new Color(200, 255, 200),
            Color.PINK,
            new Color(255, 140, 255),
            Color.MAGENTA,
            new Color(255, 0, 140),
            Color.RED,
            new Color(255, 100, 0),
            Color.ORANGE,
            new Color(255, 225, 0),
            Color.YELLOW,
            new Color(255, 255, 150),
            Color.WHITE};
    }

    private void getCentre() {
        if (xSize%2 == 0) {
            horizCenter = xSize * datapointRenderSize / 2 + datapointRenderSize / 2;
        } else {
            horizCenter = xSize * datapointRenderSize / 2;
        }
        if (ySize%2 == 0) {
            vertCenter = ySize * datapointRenderSize / 2 + datapointRenderSize / 2;
        } else {
            vertCenter = ySize * datapointRenderSize / 2;
        }
    }


    private void normalisePlotData () {
        double min = Double.MAX_VALUE;
        for (int row=0; row<ySize; row++) {
            for (int col=0; col<xSize; col++) {
                if (plotData[row][col] < min) {
                    min = plotData[row][col];
                }
            }
        }
        for (int row=0; row<ySize; row++) {
            for (int col=0; col<xSize; col++) {
                plotData[row][col] = plotData[row][col] - min;
            }
        }
        double max = -Double.MAX_VALUE;
        for (int row=0; row<ySize; row++) {
            for (int col=0; col<xSize; col++) {
                if (plotData[row][col] > max)
                    max = plotData[row][col];
            }
        }
        for (int row=0; row<ySize; row++) {
            for (int col=0; col<xSize; col++) {
                plotData[row][col] =
                    plotData[row][col] * 255 / max; // 255 reflects colour range
            }
        }
    }

    public static void main (String[] args) {

        Integer xSize = 30;
        Integer ySize = 30;
        double[][] testInput = new double[ySize][xSize];
        int datapointRenderSize = 20;

        for (int row = 0; row < ySize; row++) {
            for (int col = 0; col < xSize; col++) {
                int xSquare = col * col;
                int ySquare = row * row;
                testInput[row][col] = xSquare + ySquare;
            }
        }
        new Basic2DPlotter(testInput, datapointRenderSize, true, 0, "");
        new Basic2DPlotter(testInput, datapointRenderSize, false, 1, "");
        new Basic2DPlotter(testInput, datapointRenderSize, true, 2, "");
        new Basic2DPlotter(testInput, datapointRenderSize, true, 3, "");
        new Basic2DPlotter(testInput, datapointRenderSize, false, 4, "");
        new Basic2DPlotter(testInput, datapointRenderSize, true, 5, "");
    }
}
