package io.improbable.keanu.vertices.dbl;

import java.util.Map;
import java.util.Set;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

public interface Differentiable {
    DualNumber calculateDualNumber(Map<Differentiable, DualNumber> dualNumbers);
    default DualNumber getDualNumber() {
        return new Differentiator().calculateDual(this);
    }
    public Set<Vertex> getParents();
}
