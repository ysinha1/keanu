package io.improbable.keanu.ABM.crowd;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class LineSegment {

    private Vector3D start;
    private Vector3D end;
    private Line line;

    public LineSegment(Vector3D start, Vector3D end, double tolerance) {
        this.start = start;
        this.end = end;
        this.line = new Line(start, end, tolerance);
    }

    public Line getLine() {
        return line;
    }

    public double minimumDistance(Vector3D p) {
        double abscissa = line.getAbscissa(p);

        if (abscissa >= 1.0) {
            return end.distance(p);
        } else if (abscissa > 0.0) {
            return line.distance(p);
        } else {
            return start.distance(p);
        }
    }

    public double abscissa(Vector3D p) {
        double abscissa = line.getAbscissa(p);

        if (abscissa >= 1.0) {
            return end;
        } else if (abscissa > 0.0) {
            return line.pointAt(abscissa);
        } else {
            return start;
        }
    }

    public Vector3D closestPointOnLineSegment(Vector3D p) {
        double abscissa = line.getAbscissa(p);

        if (abscissa >= 1.0) {
            return end;
        } else if (abscissa > 0.0) {
            return line.pointAt(abscissa);
        } else {
            return start;
        }
    }
}
