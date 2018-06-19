package io.improbable.keanu.ABM.crowd.fxVis;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public abstract class FreeCam3DApplication extends Application {

    private FreeCam freeCam;
    private long targetFrameDurationMillis = 1000 / 60;

    protected final String title;
    protected final int WIDTH;
    protected final int HEIGHT;
    protected final Paint background;
    private double cameraStartX = 0.0;
    private double cameraStartY = 0.0;
    private double cameraStartZ = 0.0;

    private long lastFrameEndMilli = System.currentTimeMillis();
    private long lastFrameDurationMillis = 0;


    public FreeCam3DApplication(String title, int width, int height, Paint background) {

        this.title = title;
        this.WIDTH = width;
        this.HEIGHT = height;
        this.background = background;
    }

    public FreeCam3DApplication(String title, int width, int height, Paint background,
                                double cameraStartX, double cameraStartY, double cameraStartZ) {

        this(title, width, height, background);
        this.cameraStartX = cameraStartX;
        this.cameraStartY = cameraStartY;
        this.cameraStartZ = cameraStartZ;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle(title);
        Xform root = new Xform();
        Scene scene = createScene(root);
        createFreeCam(scene, root);

        initialiseScene(root);

        stage.setScene(scene);
        stage.show();

        startAnimator();
    }

    public abstract void initialiseScene(Xform root);

    public abstract void updateAnimation(long nano);

    protected void addToOnKeyPressed(KeyEvent event) {
        // Can override to add specific functionality
    }

    protected void addToOnMousePressed(MouseEvent event) {
        // Can override to add specific functionality
    }

    protected void addToOnMouseDragged(MouseEvent event) {
        // Can override to add specific functionality
    }

    private void startAnimator() {
        AnimationTimer animator = new AnimationTimer() {

            @Override
            public void handle(long nano) {
                freeCam.onUpdate((double) lastFrameDurationMillis / targetFrameDurationMillis);
                updateAnimation(nano);
                smoothFrameRate();
            }
        };
        animator.start();
    }

    private Scene createScene(Xform root) {
        Scene scene = new Scene(root, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        scene.setFill(background);
        return scene;
    }

    private void createFreeCam(Scene scene, Xform root) {
        double x = WIDTH / 2.0 + cameraStartX;
        double y = HEIGHT / 2.0 + cameraStartY;
        double z = cameraStartZ;

        freeCam = new FreeCam(scene, root, this::addToOnKeyPressed, x, y, z,
                this::addToOnMousePressed, this::addToOnMouseDragged);
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
