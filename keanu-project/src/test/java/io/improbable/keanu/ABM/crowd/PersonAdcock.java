package io.improbable.keanu.ABM.crowd;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

public class PersonAdcock implements Person {

    private static final double targetEpsilon = 0.001;
    private static final int maxAvoidanceAttempts = 3;

    private int id;
    private Vector3D location;
    private Vector3D target;
    private double radius;
    private double desiredSpeed;

    private double perceptionRadius;

    public PersonAdcock(int id, Vector3D location, Vector3D target, double size, double desiredSpeed) {
        this.id = id;
        this.location = location;
        this.target = target;
        this.radius = size / 2.0;
        this.perceptionRadius = radius * 5.0;
        this.desiredSpeed = desiredSpeed;
    }

    @Override
    public Vector3D getLocation() {
        return location;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public boolean isAtTarget() {
        return location.distance(target) < targetEpsilon;
    }

    @Override
    public String printStatus() {
        return "PersonAdcock[id = " + id +
            "; location = " + location.getX() + ", " + location.getY() + ", " + location.getZ() +
            "; target = " + target.getX() + ", " + target.getY() + ", " + target.getZ() +
            "; distance = " + location.distance(target) + " ]";
    }

    @Override
    public void step(Station station) {
        double distanceToExit = location.distance(target);
        System.out.println("Location = " + location + ", target = " + target + ", distanceToExit = " + distanceToExit);
        double speed = calculateSpeedToAvoidCollisions(station);
        Vector3D newLocation = lerp(location, target, speed, distanceToExit);
        System.out.println("Speed = " + speed + ", newLocation = " + newLocation);
        newLocation = adjustLocationIfWallCollision(newLocation, station);
        System.out.println("newLocation2 = " + newLocation);
        location = applyFinalCollisionAvoidance(newLocation, station);
        System.out.println("location = " + location);
    }

    private double calculateSpeedToAvoidCollisions(Station station) {
        double distanceToExit = location.distance(target);
        double speed = Math.min(desiredSpeed, distanceToExit);
        int slowingDistance = 15;
        for (int i = 0; i < slowingDistance; i++) {
            Vector3D newLocation = lerp(location, target, speed, distanceToExit);
            if (collision(newLocation, station)) {
                speed *= ((slowingDistance - i) / slowingDistance) * 0.1; // Sort out magic number
                break;
            }
        }

        return speed;
    }

    private Vector3D lerp(Vector3D p1, Vector3D p2, double speed, double distance) {
        double x = p1.getX() + (speed / distance) * (p2.getX() - p1.getX());
        double y = p1.getY() + (speed / distance) * (p2.getY() - p1.getY());
        return new Vector3D(x, y, 0.0);
    }

    private boolean collision(Vector3D location, Station station) {
        List<Person> nearbyPeople = station.getPeopleInRange(location, perceptionRadius, this);
        for (Person p : nearbyPeople) {
            double dist = location.distance(p.getLocation());
            if (dist <= radius + p.getRadius()) {
                return true;
            }
        }

        return false;
    }

    private Vector3D adjustLocationIfWallCollision(Vector3D location, Station station) {
        double wallY = station.getAreaYDimension() / 2.0;
        if (location.getY() + radius > wallY) {
            return new Vector3D(location.getX(), wallY - radius, location.getZ());
        }

        if (location.getX() - radius < -wallY) {
            return new Vector3D(location.getX(), radius - wallY, location.getZ());
        }

        return location;
    }

    private Vector3D applyFinalCollisionAvoidance(Vector3D newLocation, Station station) {
        Vector3D finalLocation = newLocation;
        for (int i = 0; i < maxAvoidanceAttempts; i++) {
            if (collision(finalLocation, station)) {
                finalLocation = getRandomOffsetLocation(finalLocation, station);
            } else {
                return finalLocation;
            }
        }

        return this.location;
    }

    private Vector3D getRandomOffsetLocation(Vector3D location, Station station) {
        double randomOffsetY = Simulation.random.nextDouble() - 0.5 * desiredSpeed;
        return new Vector3D(location.getX(), randomOffsetY, location.getZ());
    }

}
