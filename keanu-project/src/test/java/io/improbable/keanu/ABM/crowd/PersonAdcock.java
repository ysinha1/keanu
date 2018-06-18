package io.improbable.keanu.ABM.crowd;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

public class PersonAdcock implements Person {

    private Vector3D location;
    private Vector3D target;
    private double radius;
    private double desiredSpeed;
    private int maxAvoidanceAttempts = 3;

    private double perceptionRadius;

    public PersonAdcock(Vector3D location, Vector3D target, double size, double desiredSpeed) {
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
    public void step(Station station) {
        double distanceToExit = location.distance(target);
        double speed = calculateSpeedToAvoidCollisions(station);
        Vector3D newLocation = lerp(location, target, speed, distanceToExit);
        newLocation = adjustLocationIfWallCollision(newLocation, station);
        location = applyFinalCollisionAvoidance(newLocation, station);
    }

    private double calculateSpeedToAvoidCollisions(Station station) {
        double distanceToExit = location.distance(target);
        double speed = desiredSpeed;
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
        double wallX = station.getAreaXDimension() / 2.0;
        if (location.getX() + radius > wallX) {
            return new Vector3D(wallX - radius, location.getY(), location.getZ());
        }

        if (location.getX() - radius < -wallX) {
            return new Vector3D(radius - wallX, location.getY(), location.getZ());
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
        double randomOffsetY = station.getRandom().nextDouble() - 0.5 * desiredSpeed;
        return new Vector3D(location.getX(), randomOffsetY, location.getZ());
    }

}
