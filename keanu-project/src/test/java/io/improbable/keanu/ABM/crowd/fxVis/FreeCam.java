package io.improbable.keanu.ABM.crowd.fxVis;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

import static java.lang.Math.sqrt;

public class FreeCam {

    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 1000000.0;

    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double SHIFT_MULTIPLIER = 10.0;

    private static final double MOUSE_ROTATION_SPEED = 1.0;
    private static final double D_PAD_ROTATION_SPEED = 1.0;

    private static final double X_AXIS_MOVE_SPEED = 3.0;
    private static final double Y_AXIS_MOVE_SPEED = 3.0;
    private static final double Z_AXIS_MOVE_SPEED = 3.0;

    private final Xform cameraMount = new Xform();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    private final BooleanProperty wPressed = new SimpleBooleanProperty(false);
    private final BooleanProperty aPressed = new SimpleBooleanProperty(false);
    private final BooleanProperty sPressed = new SimpleBooleanProperty(false);
    private final BooleanProperty dPressed = new SimpleBooleanProperty(false);
    private final BooleanProperty qPressed = new SimpleBooleanProperty(false);
    private final BooleanProperty ePressed = new SimpleBooleanProperty(false);

    private final BooleanProperty upPressed = new SimpleBooleanProperty(false);
    private final BooleanProperty downPressed = new SimpleBooleanProperty(false);
    private final BooleanProperty leftPressed = new SimpleBooleanProperty(false);
    private final BooleanProperty rightPressed = new SimpleBooleanProperty(false);

    private final BooleanProperty shiftPressed = new SimpleBooleanProperty(false);
    private final BooleanProperty controlPressed = new SimpleBooleanProperty(false);

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    public FreeCam(Scene scene, Xform root, Consumer<KeyEvent> addToOnKeyPressed, double initialX, double initialY,
                   double initialZ, Consumer<MouseEvent> addToOnMousePressed, Consumer<MouseEvent> addToOnMouseDragged) {

        initializeCamera(scene, root, initialX, initialY, initialZ);
        setUpControls(scene, addToOnKeyPressed, addToOnMousePressed, addToOnMouseDragged);
    }

    public void onUpdate(double frameRateComparedToTarget) {
        double speedModifier = getSpeedModifier(shiftPressed.get(), controlPressed.get());
        speedModifier /= frameRateComparedToTarget;

        if (wPressed.get()) {
            moveForward(Z_AXIS_MOVE_SPEED * speedModifier);
        }

        if (sPressed.get()) {
            moveForward(-Z_AXIS_MOVE_SPEED * speedModifier);
        }

        if (dPressed.get()) {
            moveRight(X_AXIS_MOVE_SPEED * speedModifier);
        }

        if (aPressed.get()) {
            moveRight(-X_AXIS_MOVE_SPEED * speedModifier);
        }

        if (ePressed.get()) {
            moveUp(Y_AXIS_MOVE_SPEED * speedModifier);
        }

        if (qPressed.get()) {
            moveUp(-Y_AXIS_MOVE_SPEED * speedModifier);
        }

        if (upPressed.get()) {
            cameraMount.setRotateX(cameraMount.getRotateX() + D_PAD_ROTATION_SPEED);
        }

        if (downPressed.get()) {
            cameraMount.setRotateX(cameraMount.getRotateX() - D_PAD_ROTATION_SPEED);
        }

        if (leftPressed.get()) {
            cameraMount.setRotateY(cameraMount.getRotateY() - D_PAD_ROTATION_SPEED);
        }

        if (rightPressed.get()) {
            cameraMount.setRotateY(cameraMount.getRotateY() + D_PAD_ROTATION_SPEED);
        }
    }

    private void initializeCamera(Scene scene, Xform root, double initialX, double initialY, double initialZ) {
        root.getChildren().add(cameraMount);
        cameraMount.getChildren().add(camera);
        cameraMount.setTranslate(initialX, initialY, initialZ);

        camera.setFieldOfView(45.0);
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);

        scene.setCamera(camera);
    }

    private void setUpControls(Scene scene, Consumer<KeyEvent> addToOnKeyPressed,
                               Consumer<MouseEvent> addToOnMousePressed, Consumer<MouseEvent> addToOnMouseDragged) {

        setupKeyboardControls(scene, addToOnKeyPressed);
        setupMouseControls(scene, addToOnMousePressed, addToOnMouseDragged);
    }

    private void setupKeyboardControls(Scene scene, Consumer<KeyEvent> addToOnKeyPressed) {
        scene.setOnKeyPressed((KeyEvent event) -> {
            checkAndSetKeyboardControlsChanged(event, true);
            addToOnKeyPressed.accept(event);
        });

        scene.setOnKeyReleased((KeyEvent event) -> {
            checkAndSetKeyboardControlsChanged(event, false);
        });
    }

    private void checkAndSetKeyboardControlsChanged(KeyEvent event, boolean isPressed) {
        BooleanProperty keyBooleanProperty = getKeyBooleanProperty(event.getCode());
        if (keyBooleanProperty != null) {
            keyBooleanProperty.setValue(isPressed);
        }
    }

    private BooleanProperty getKeyBooleanProperty(KeyCode keyCode) {
        switch (keyCode) {
            case W:
                return wPressed;
            case S:
                return sPressed;
            case D:
                return dPressed;
            case A:
                return aPressed;
            case E:
                return ePressed;
            case Q:
                return qPressed;
            case UP:
                return upPressed;
            case DOWN:
                return downPressed;
            case LEFT:
                return leftPressed;
            case RIGHT:
                return rightPressed;
            case SHIFT:
                return shiftPressed;
            case CONTROL:
                return controlPressed;
            default:
                return null;
        }
    }

    private void moveForward(double dist) {
        double yAxisDegrees = cameraMount.getRotateY();
        moveRelativeToRotationAngle2d(yAxisDegrees, dist);
    }

    private void moveRight(double dist) {
        double yAxisDegrees = cameraMount.getRotateY() + 90.0;
        moveRelativeToRotationAngle2d(yAxisDegrees, dist);
    }

    private void moveUp(double dist) {
        cameraMount.setTy(cameraMount.getTy() - dist);
    }

    private void moveRelativeToRotationAngle2d(double yAxisDegrees, double dist) {
        double yAxisDeg = yAxisDegrees % 360.0;
        double yAxisRad = Math.toRadians(yAxisDeg);

        double z = Math.cos(yAxisRad) * dist;
        double x = Math.sin(yAxisRad) * dist;

        cameraMount.setTx(cameraMount.getTx() + x);
        cameraMount.setTz(cameraMount.getTz() + z);
    }

    private void moveRelativeToRotationAngle(double xAxisDegrees, double yAxisDegrees, double dist) {
        double xAxisDeg = xAxisDegrees % 360.0;
        double yAxisDeg = yAxisDegrees % 360.0;
        double xAxisRad = Math.toRadians(xAxisDeg);
        double yAxisRad = Math.toRadians(yAxisDeg);

        double z2d = Math.cos(yAxisRad);
        double x2d = Math.sin(yAxisRad);
        double z = z2d * Math.cos(xAxisRad) * dist;
        double x = x2d * Math.cos(xAxisRad) * dist;
        double y = -sqrt((x2d * x2d) + (z2d * z2d)) * Math.sin(xAxisRad) * dist; // negative because y is inverted

        cameraMount.setTx(cameraMount.getTx() + x);
        cameraMount.setTy(cameraMount.getTy() + y);
        cameraMount.setTz(cameraMount.getTz() + z);
    }

    private double getSpeedModifier(boolean isShiftDown, boolean isControlDown) {
        if (isControlDown) {
            return CONTROL_MULTIPLIER;
        }

        if (isShiftDown) {
            return SHIFT_MULTIPLIER;
        }

        return 1.0;
    }

    private void setupMouseControls(Scene scene, Consumer<MouseEvent> addToOnMousePressed,
                                    Consumer<MouseEvent> addToOnMouseDragged) {

        scene.setOnMousePressed((MouseEvent event) -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();

            addToOnMousePressed.accept(event);
        });

        scene.setOnMouseDragged((MouseEvent event) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double speedModifier = getSpeedModifier(event.isShiftDown(), event.isControlDown());

            if (event.isSecondaryButtonDown()) {
                double xAxisAngle = cameraMount.getRotateX() + (mouseDeltaY * MOUSE_ROTATION_SPEED * speedModifier);
                double yAxisAngle = cameraMount.getRotateY() + (mouseDeltaX * MOUSE_ROTATION_SPEED * speedModifier);
                cameraMount.setRotateX(xAxisAngle);
                cameraMount.setRotateY(yAxisAngle);
            }

            addToOnMouseDragged.accept(event);
        });
    }
}
