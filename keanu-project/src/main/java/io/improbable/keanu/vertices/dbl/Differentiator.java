package io.improbable.keanu.vertices.dbl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

public class Differentiator {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public <V extends Differentiable> DualNumber calculateDual(V vertex) {
        log.trace("calculateDual({})", vertex);
        Map<Differentiable, DualNumber> dualNumbers = new HashMap<>();
        Deque<V> stack = new ArrayDeque<>();
        stack.push(vertex);

        while (!stack.isEmpty()) {

            V head = stack.peek();
            Set<Vertex> parentsThatDualNumberIsNotCalculated = parentsThatDualNumberIsNotCalculated(dualNumbers, head.getParents());

            if (parentsThatDualNumberIsNotCalculated.isEmpty()) {

                Differentiable top = stack.pop();
                log.trace("calculateDualNumber: {}", top);
                DualNumber dual = top.calculateDualNumber(dualNumbers);
                log.trace(dual.getPartialDerivatives().toString());
                dualNumbers.put(top, dual);

            } else {

                for (Vertex parent : parentsThatDualNumberIsNotCalculated) {
                    if (parent instanceof Differentiable) {
                        stack.push((V) parent);
                    } else {
                        throw new IllegalArgumentException("Can only calculate Dual Numbers on a graph made of Differentiable vertices");
                    }
                }

            }

        }

        return dualNumbers.get(vertex);
    }

    private Set<Vertex> parentsThatDualNumberIsNotCalculated(Map<Differentiable, DualNumber> dualNumbers, Set<? extends Vertex> parents) {
        Set<Vertex> notCalculatedParents = new HashSet<>();
        for (Vertex next : parents) {
            if (!dualNumbers.containsKey(next)) {
                notCalculatedParents.add(next);
            }
        }
        return notCalculatedParents;
    }
}
