package io.improbable.keanu.ABM.crowd;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;
import java.util.Optional;

public class PersonIntermediate implements Person {

    private double targetEpsilon = 0.001;
    private static Vector3D zAxis = Vector3D.PLUS_K;
    private static double maxRotation = Math.PI / 2.0;

    private int id;
    private Vector3D location;
    private Vector3D target;
    private double radius;
    private double desiredSpeed;

    private double currentSpeed;

    public PersonIntermediate(int id, Vector3D location, Vector3D target, double size, double desiredSpeed) {
        this.id = id;
        this.location = location;
        this.target = target;
        this.radius = size / 2.0;
        this.desiredSpeed = desiredSpeed;
    }

    @Override
    public int getId() {
        return id;
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
        return "PersonAdcock[" +
            "location = " + location.getX() + ", " + location.getY() + ", " + location.getZ() +
            "; target = " + target.getX() + ", " + target.getY() + ", " + target.getZ() +
            "; distance = " + location.distance(target) + " ]";
    }

    @Override
    public void step(Station station) {
        double speed = desiredSpeed;
        Vector3D nextLocation = calculateNextLocation(speed, 0.0);

        Optional<Person> willCollideWith = detectCollision(location, nextLocation, station);
        if (willCollideWith.isPresent()) {
            Vector3D desiredVector = nextLocation.subtract(location);
            Person p = willCollideWith.get();
            desiredVector = vectorPastPerson(p, speed, desiredVector);
            nextLocation = location.add(desiredVector);
        }
    }

    private Vector3D calculateNextLocation(double dist, double offsetAngle) {
        Vector3D moveVector = target.subtract(location).normalize().scalarMultiply(dist);
        if (offsetAngle == 0.0) {
            return moveVector;
        }

        return rotateAroundZAxis(moveVector, offsetAngle);
    }

    private Optional<Person> detectCollision(Vector3D moveStart, Vector3D moveEnd, Station station) {
        LineSegment move = new LineSegment(moveStart, moveEnd, 1e-20);
        double nearbyRadius = moveStart.distance(moveEnd) + radius * 2;
        List<Person> nearbyPeople = station.getPeopleInRange(location, nearbyRadius, this);
        Optional<Person> collidedWithPerson = Optional.empty();
        double minCollisionAbscissa = Double.MAX_VALUE;

        for (Person p : nearbyPeople) {
            double minDistForNoCollision = radius + p.getRadius();
            double distance = move.minimumDistance(p.getLocation());
            if (distance < minDistForNoCollision) {
                double collisionAbscissa = move.getLine().getAbscissa(p.getLocation());
                if (collisionAbscissa < minCollisionAbscissa) {
                    collidedWithPerson = Optional.of(p);
                    minCollisionAbscissa = collisionAbscissa;
                }
            }
        }

        return collidedWithPerson;
    }

    private Vector3D vectorPastPerson(Person p, double speed, Vector3D desiredVector) {
        double dist = p.getLocation().distance(location);
        double safeRadius = radius + p.getRadius();
        double angle = Math.asin(safeRadius / dist);
        Vector3D vectorTowardsPerson = p.getLocation().subtract(location).normalize().scalarMultiply(speed);
        Vector3D tangent1 = rotateAroundZAxis(vectorTowardsPerson, angle);
        Vector3D tangent2 = rotateAroundZAxis(vectorTowardsPerson, -angle);

        if (Vector3D.angle(desiredVector, tangent1) <= Vector3D.angle(desiredVector, tangent2)) {
            return tangent1;
        }

        return tangent2;
    }

    private Vector3D rotateAroundZAxis(Vector3D vectorToRotate, double angle) {
        Rotation rotation = new Rotation(zAxis, 0.0, RotationConvention.VECTOR_OPERATOR);
        return rotation.applyTo(vectorToRotate);
    }

    public static void main(String[] args) {

        Vector3D a = new Vector3D(0.0, 0.0, 0.0);
        Vector3D b = new Vector3D(1.0, 0.0, 0.0);
        Vector3D x = new Vector3D(2.0, 1.0, 0.0);

        Line ab = new Line(a, b, 1e-15);
        System.out.println("Abscissa = " + ab.getAbscissa(x));
        System.out.println("Dist = " + ab.distance(x));
    }
}
