package io.improbable.keanu.ABM.crowd.model;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Utils {

    public static boolean intersect(Vector3D a, double radiusA, Vector3D b, double radiusB) {
        return a.distance(b) <= radiusA + radiusB;
    }
}
