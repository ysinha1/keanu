package io.improbable.keanu.ABM;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class testPlotting extends Frame {

    int xSize;
    int ySize;
    int datapointRenderSize;
    boolean axis;
    double[][] plotData;
    public String title;

    int horizCenter;
    int vertCenter;

    public class GreyscalePlotCanvas extends Canvas {

        public GreyscalePlotCanvas() {
            setSize(xSize*datapointRenderSize, ySize*datapointRenderSize);
        }

        public void paint(Graphics g) {
            Color colour;
            for (int row=0; row<ySize; row++) {
                for (int col=0; col<xSize; col++) {
                    int red = (int) plotData[row][col];
                    int green = red;
                    int blue = red;
                    colour = new Color(red, green, blue);
                    g.setColor(colour);
                    g.fillRect(col*datapointRenderSize, row*datapointRenderSize, datapointRenderSize, datapointRenderSize);
                }
            }
            if(axis) {
                g.setColor(Color.RED);
                g.drawLine(0, vertCenter, 2*horizCenter, vertCenter);
                g.drawLine(horizCenter, 0, horizCenter, 2*vertCenter);
            }
        }
    }

    public class GreyscalePlotScale extends Canvas {
        int scaleHeight = 6 * datapointRenderSize;

        public GreyscalePlotScale () {
            setSize(xSize*datapointRenderSize, scaleHeight);
        }

        public void paint(Graphics g) {
            Color colour;
            for (int row = 1; row < scaleHeight; row++) {
                for (int col = 0; col < xSize; col++) {
                    int scaleValue = 255 * col / (xSize - 1);
                    int red = scaleValue;
                    int green = red;
                    int blue = red;
                    colour = new Color(red, green, blue);
                    g.setColor(colour);
                    g.fillRect(col * datapointRenderSize, row * datapointRenderSize, datapointRenderSize, datapointRenderSize);
                }
            }
        }
    }

    public class ColourshiftPlotCanvas extends Canvas {

        public ColourshiftPlotCanvas() {
            setSize(xSize * datapointRenderSize, ySize * datapointRenderSize);
        }

        public void paint(Graphics g) {
            Color colour;
            for (int row = 0; row < ySize; row++) {
                for (int col = 0; col < xSize; col++) {
                    int red = 0;
                    int green = 0;
                    int blue = 0;
                    if ((int) plotData[row][col] == 255) {
                        red = green = blue = 255;
                    } else if ((int) plotData[row][col] == 0) {
                        red = green = blue = 0;
                    } else if (((int) plotData[row][col] > 0) && ((int) plotData[row][col] <= 63)) {
                        int temp = 4 * ((int) plotData[row][col]);
                        blue = 255;
                        green = temp;
                    } else if(((int)plotData[row][col] > 63) && ((int)plotData[row][col] <= 127)) {
                        int temp = 4 * ((int) plotData[row][col] - 64);
                        green = 255;
                        blue = 255 - temp;
                    } else if (((int) plotData[row][col] > 127) && ((int) plotData[row][col] <= 191)) {
                        int temp = 4 * ((int) plotData[row][col] - 128);
                        green = 255;
                        red = temp;
                    } else if (((int) plotData[row][col] > 191) && ((int) plotData[row][col] <= 254)) {
                        int temp = 4 * ((int)  plotData[row][col] - 192);
                        red = 255;
                        green = 255 - temp;
                    }
                    colour = new Color(red, green, blue);
                    g.setColor(colour);
                    g.fillRect(col * datapointRenderSize, row * datapointRenderSize, datapointRenderSize, datapointRenderSize);
                }
            }
        }
    }

    public class ColourshiftPlotScale extends Canvas {
        int scaleHeight = 6 * datapointRenderSize;

        ColourshiftPlotScale() {
            setSize(xSize * datapointRenderSize, scaleHeight);
        }

        public void paint(Graphics g) {
            Color color;
            for (int row = 1; row < scaleHeight; row++) {
                for (int col = 0; col < xSize; col++) {
                    int scaleValue = 255 * col / (xSize - 1);
                    int red = 0;
                    int green = 0;
                    int blue = 0;
                    if (scaleValue == 255) {
                        red = green = blue = 255;
                    } else if ((scaleValue > 0) && (scaleValue <= 63)) {
                        scaleValue = 4 * scaleValue;
                        blue = 255;
                        green = scaleValue;
                    } else if ((scaleValue > 63) && (scaleValue <= 127)) {
                        scaleValue = 4 * (scaleValue - 64);
                        green = 255;
                        blue = 255 - scaleValue;
                    } else if ((scaleValue > 127) && (scaleValue <= 191)) {
                        scaleValue = 4 * (scaleValue - 128);
                        green = 255;
                        red = scaleValue;
                    } else if ((scaleValue > 191) && (scaleValue <= 254)) {
                        scaleValue = 4 * (scaleValue - 192);
                        red = 255;
                        green = 255 - scaleValue;
                    }
                    color = new Color(red, green, blue);
                    g.setColor(color);
                    g.fillRect(col * datapointRenderSize, row * datapointRenderSize, datapointRenderSize, datapointRenderSize);
                }
            }
        }
    }

    Color[] getColourPalette() {
        Color[] colourPalette = {
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
        return colourPalette;
    }

    public class ColourContourCanvas extends Canvas {

        public ColourContourCanvas() { setSize(xSize * datapointRenderSize, ySize * datapointRenderSize); }

        public void paint(Graphics g) {
            Color[] colorPalette = getColourPalette();
            for (int row=0; row<ySize; row++) {
                for (int col=0; col<xSize; col++) {
                    int quantizedData = (int) (Math.round(plotData[row][col] * (colorPalette.length - 1) / 255));
                    g.setColor(colorPalette[quantizedData]);
                    g.fillRect(col*datapointRenderSize, row*datapointRenderSize, datapointRenderSize, datapointRenderSize);
                }
            }
            if (axis) {
                g.setColor(Color.RED);
                g.drawLine(0, vertCenter, 2 * horizCenter, vertCenter);
                g.drawLine(horizCenter, 0, horizCenter, 2 * vertCenter);
            }
        }
    }

    public class ColourContourScale extends Canvas {
        int scaleHeight = 6 * datapointRenderSize;

        public ColourContourScale() { setSize(xSize*datapointRenderSize, scaleHeight); }

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

    public testPlotting(double[][] dataIn,int datapointRenderSize, boolean axis, int display, String title) {
        this.datapointRenderSize = datapointRenderSize;
        this.axis = axis;
        ySize = dataIn.length;
        xSize = dataIn[0].length;
        this.title = title;

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


    public void getCenter() {
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


    public void normalisePlotData () {
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
        new testPlotting(testInput, datapointRenderSize, true, 0, "");
        new testPlotting(testInput, datapointRenderSize, false, 1, "");
        new testPlotting(testInput, datapointRenderSize, true, 2, "");
        new testPlotting(testInput, datapointRenderSize, true, 3, "");
        new testPlotting(testInput, datapointRenderSize, false, 4, "");
        new testPlotting(testInput, datapointRenderSize, true, 5, "");
    }
}
