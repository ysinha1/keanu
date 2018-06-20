package io.improbable.keanu.ABM.crowd;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;
import java.util.stream.Collectors;

public class Station {

    private double areaYDimension;
    private List<Person> people = new ArrayList<>();

    public Station(double areaYDimension) {
        this.areaYDimension = areaYDimension;
    }

    public double getAreaYDimension() {
        return areaYDimension;
    }

    public List<Person> getPeople() {
        return people;
    }

    public void add(Person person) {
        people.add(person);
    }

    public List<Person> getPeopleInRange(Vector3D location, double range, Person... excluding) {
        return people.stream().filter(p ->
            !personInArray(p, excluding) && location.distance(p.getLocation()) <= range
        ).collect(Collectors.toList());
    }

    public void removePeopleAtDestinations() {
        int i = 0;
        while (i < people.size()) {
            if (people.get(i).isAtTarget()) {
                people.remove(i);
            }
            i++;
        }
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
