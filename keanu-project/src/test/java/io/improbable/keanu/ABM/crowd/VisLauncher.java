package io.improbable.keanu.ABM.crowd;

import io.improbable.keanu.ABM.crowd.fxVis.FreeCam3DApplication;
import io.improbable.keanu.ABM.crowd.fxVis.Xform;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Test;

import java.util.*;

public class VisLauncher extends FreeCam3DApplication {

    private PhongMaterial green = new PhongMaterial();
    private PhongMaterial red = new PhongMaterial();
    private PhongMaterial blue = new PhongMaterial();
    private PhongMaterial yellow = new PhongMaterial();
    private PhongMaterial grey = new PhongMaterial();
    private List<PhongMaterial> personMaterials;
    private Xform middle;

    private Simulation sim = new Simulation();
    private int nextPersonMaterialIdx = 0;
    private Map<Integer, Cylinder> cylinders = new HashMap<>();
    private long updateFrequencyMillis = 100;
    private long updateFrequencyNanos = updateFrequencyMillis * 1000000;
    private long lastUpdateNano = 0;

    public VisLauncher() {
        super("Crowd", 1000, 800, Color.WHITE, 50.0, 0.0, -150.0);
    }

    @Test
    public void runTest() {
        launch();
    }

    @Override
    public void initialiseScene(Xform root) {
        initMaterials();
        middle = createMiddle(root);
        createOriginAndDestinationPoints(middle);
    }

    @Override
    public void updateAnimation(long nano) {
        if (nano - lastUpdateNano >= updateFrequencyNanos) {
            lastUpdateNano = nano;
            sim.step();

            Set<Integer> activeIds = new HashSet<>();
            for (Person person : sim.getPeople()) {
                int id = person.getId();
                activeIds.add(id);
                Vector3D location = person.getLocation();

                if (cylinders.containsKey(id)) {
                    Cylinder cylinder = cylinders.get(id);
                    setLocation(cylinder, location);
                } else {
                    Cylinder cylinder = createCylinder(location);
                    middle.getChildren().add(cylinder);
                    cylinders.put(id, cylinder);
                }
            }

            removeInactive(activeIds);
        }
    }

    private void initMaterials() {
        green.setSpecularColor(Color.GREEN);
        green.setDiffuseColor(Color.GREEN);
        red.setSpecularColor(Color.RED);
        red.setDiffuseColor(Color.RED);
        blue.setSpecularColor(Color.BLUE);
        blue.setDiffuseColor(Color.BLUE);
        yellow.setSpecularColor(Color.YELLOW);
        yellow.setDiffuseColor(Color.YELLOW);
        grey.setSpecularColor(Color.GREY);
        grey.setDiffuseColor(Color.GREY);
        personMaterials = Arrays.asList(green, red, blue, yellow);
    }

    private Xform createMiddle(Xform root) {
        Xform middle = new Xform();
        middle.setTranslateX(WIDTH / 2.0);
        middle.setTranslateY(HEIGHT / 2.0);
        middle.setTranslateZ(0.0);
        root.getChildren().add(middle);
        return middle;
    }

    private void createOriginAndDestinationPoints(Xform middle) {
        for (Vector3D origin : sim.getOrigins()) {
            Box box = createBox(origin);
            middle.getChildren().add(box);
        }

        for (Vector3D destination : sim.getDestinations()) {
            Box box = createBox(destination);
            middle.getChildren().add(box);
        }
    }

    private Box createBox(Vector3D pos) {
        Box box = new Box();
        setLocation(box, pos);
        box.setWidth(1.0);
        box.setHeight(3.0);
        box.setDepth(1.0);
        box.setMaterial(grey);
        return box;
    }

    private void setLocation(Node node, Vector3D pos) {
        node.setTranslateX(pos.getX());
        node.setTranslateY(pos.getY());
        node.setTranslateZ(pos.getZ());
    }

    private void setRotation(Node node, double x, double y, double z) {
        node.setRotationAxis(new Point3D(1.0, 0.0, 0.0));
        node.setRotate(x);
        node.setRotationAxis(new Point3D(0.0, 1.0, 0.0));
        node.setRotate(y);
        node.setRotationAxis(new Point3D(0.0, 0.0, 1.0));
        node.setRotate(z);
    }

    private Cylinder createCylinder(Vector3D pos) {
        Cylinder cylinder = new Cylinder();
        setLocation(cylinder, pos);
        cylinder.setRotationAxis(new Point3D(1.0, 0.0, 0.0));
        cylinder.setRotate(90);
        cylinder.setHeight(1.8);
        cylinder.setRadius(0.5);
        cylinder.setMaterial(getNextPersonMaterial());
        return cylinder;
    }

    private PhongMaterial getNextPersonMaterial() {
        PhongMaterial mat = personMaterials.get(nextPersonMaterialIdx);
        nextPersonMaterialIdx = (nextPersonMaterialIdx + 1) % personMaterials.size();
        return mat;
    }

    private void removeInactive(Set<Integer> activeIds) {
        List<Integer> inactiveIds = new ArrayList<>();
        for (Map.Entry<Integer, Cylinder> activePerson : cylinders.entrySet()) {
            int id = activePerson.getKey();
            if (!activeIds.contains(id)) {
                inactiveIds.add(id);
            }
        }

        for (Integer inactiveId : inactiveIds) {
            Cylinder inactive = cylinders.get(inactiveId);
            middle.getChildren().remove(inactive);
            cylinders.remove(inactiveId);
        }
    }

    private String getPositionString(Node node) {
        return node.getTranslateX() + "," + node.getTranslateY() + "," + node.getTranslateZ();
    }
}
