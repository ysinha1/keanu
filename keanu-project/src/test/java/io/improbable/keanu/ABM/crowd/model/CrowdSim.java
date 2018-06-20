package io.improbable.keanu.ABM.crowd.model;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {

    public static final Random random = new Random(1);
    private static double minSpeed = 1.0;
    private static double maxSpeed = 3.0;
    private static double personSize = 2.0;
    private static double stationYDimension = 50.0;
    private static int numOrigins = 5;
    private static int numDestinations = 5;
    private static int maxPeople = 10;
    private static int timesteps = 1000;
    private static boolean walkBackAndForth = true;

    private List<Vector3D> origins;
    private List<Vector3D> destinations;
    private Station station;
    private int nextPersonId = 0;

    public Simulation() {
        station = new Station(stationYDimension);
        origins = generateOrigins();
        destinations = generateDestinations();
    }

    public List<Vector3D> getOrigins() {
        return origins;
    }

    public List<Vector3D> getDestinations() {
        return destinations;
    }

    public List<Person> getPeople() {
        return station.getPeople();
    }

    public void step() {
        for (Person person : station.getPeople()) {
            person.step(station);
        }

        if (station.getPeople().size() < maxPeople) {
            createPerson();
        }

        station.removePeopleAtDestinations();
    }

    private List<Vector3D> generateOrigins() {
        double halfYDimension = stationYDimension / 2.0;
        return generateEvenlySpacedPointsAlongY(numOrigins, halfYDimension, -halfYDimension, 0.0, 0.0);
    }

    private List<Vector3D> generateDestinations() {
        double halfYDimension = stationYDimension / 2.0;
        return generateEvenlySpacedPointsAlongY(numDestinations, halfYDimension, -halfYDimension, 100.0, 0.0);
    }

    private List<Vector3D> generateEvenlySpacedPointsAlongY(int num, double leftWallY, double rightWallY, double x, double z) {
        double interval = (leftWallY - rightWallY) / (num + 1);
        List<Vector3D> points = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            double y = rightWallY + interval + (interval * i);
            points.add(new Vector3D(x, y, z));
        }

        return points;
    }

    private void createPerson() {
        Vector3D origin = selectRandom(origins);
        Vector3D destination = selectRandom(destinations);
        double desiredSpeed = randomSpeed();
        station.getPeople().add(new PersonAdcock(nextPersonId++, origin, destination, personSize, desiredSpeed, walkBackAndForth));
    }

    private <T> T selectRandom(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private double randomSpeed() {
        return randomDouble(minSpeed, maxSpeed);
    }

    private double randomDouble(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    public static void main(String[] args) {
        Simulation sim = new Simulation();

        for (int i = 0; i < timesteps; i++) {
            sim.step();
        }
    }
}
