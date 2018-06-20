package io.improbable.keanu.ABM.crowd;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Person {

    int getId();

    Vector3D getLocation();

    double getRadius();

    boolean isAtTarget();

    String printStatus();

    void step(Station station);
}
