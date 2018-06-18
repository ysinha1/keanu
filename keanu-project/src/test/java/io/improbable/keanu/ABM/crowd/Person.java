package io.improbable.keanu.ABM.crowd;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Person {

    Vector3D getLocation();

    double getRadius();

    void step(Station station);
}
