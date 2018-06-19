package io.improbable.keanu.ABM.crowd.fxVis;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.chart.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Map;

public abstract class ChartApplication extends Application {

    private long targetFrameDurationMillis = 1000 / 60;

    protected final String title;
    protected final int WIDTH;
    protected final int HEIGHT;
    protected final Paint background;

    private long lastFrameEndMilli = System.currentTimeMillis();
    private long lastFrameDurationMillis = 0;


    public ChartApplication(String title, int width, int height, Paint background) {

        this.title = title;
        this.WIDTH = width;
        this.HEIGHT = height;
        this.background = background;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle(title);
        Group root = new Group();
        Scene scene = createScene(root);

        initialiseScene(root);

        stage.setScene(scene);
        stage.show();

        startAnimator();
    }

    public abstract void initialiseScene(Group root);

    protected void updateAnimation(long nano) {
        // Can override to add animation functionality
    }

    protected void addToOnKeyPressed(KeyEvent event) {
        // Can override to add specific functionality
    }

    protected void addToOnMousePressed(MouseEvent event) {
        // Can override to add specific functionality
    }

    protected void addToOnMouseDragged(MouseEvent event) {
        // Can override to add specific functionality
    }

    protected Node makeHistogram(Map<String, Number> labelledData, String name, String xAxisLabel,
                                 String yAxisLabel) {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setCategoryGap(0);
        barChart.setBarGap(0);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);

        for (Map.Entry<String, Number> entry : labelledData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);

        return barChart;
    }

    protected Node makeScatterPlot(ArrayList<Pair<Number, Number>> dataPoints, String name, String xAxisLabel,
                                   String yAxisLabel) {

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        ScatterChart<Number, Number> barChart = new ScatterChart<>(xAxis, yAxis);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);

        for (Pair<Number, Number> dataPoint : dataPoints) {
            series.getData().add(new XYChart.Data<>(dataPoint.getKey(), dataPoint.getValue()));
        }

        barChart.getData().add(series);

        return barChart;
    }

    private void startAnimator() {
        AnimationTimer animator = new AnimationTimer() {

            @Override
            public void handle(long nano) {
                updateAnimation(nano);
                smoothFrameRate();
            }
        };
        animator.start();
    }

    private Scene createScene(Group root) {
        Scene scene = new Scene(root, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        scene.setFill(background);
        return scene;
    }

    private void smoothFrameRate() {
        long sinceLastFrameEndMillis = System.currentTimeMillis() - lastFrameEndMilli;
        if (sinceLastFrameEndMillis < targetFrameDurationMillis) {
            try {
                Thread.sleep(targetFrameDurationMillis - sinceLastFrameEndMillis);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        long currentTimeMillis = System.currentTimeMillis();
        lastFrameDurationMillis = currentTimeMillis - lastFrameEndMilli;
        lastFrameEndMilli = currentTimeMillis;
    }
}
