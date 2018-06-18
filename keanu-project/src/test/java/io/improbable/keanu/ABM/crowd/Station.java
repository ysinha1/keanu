package io.improbable.keanu.ABM.crowd;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;
import java.util.stream.Collectors;

public class Station {

    private Random random = new Random(1);
    private double areaXDimension = 200.0;
    private double areaYDimension = 100.0;
    private List<Person> people = new ArrayList<>();

    public Random getRandom() {
        return random;
    }

    public double getAreaXDimension() {
        return areaXDimension;
    }

    public double getAreaYDimension() {
        return areaYDimension;
    }

    public List<Person> getPeopleInRange(Vector3D location, double range, Person... excluding) {
        return people.stream().filter(p ->
            !personInArray(p, excluding) && range <= location.distance(p.getLocation())
        ).collect(Collectors.toList());
    }

    private boolean personInArray(Person p, Person[] people) {
        for (Person other : people) {
            if (p == other) {
                return true;
            }
        }
        return false;
    }
}
