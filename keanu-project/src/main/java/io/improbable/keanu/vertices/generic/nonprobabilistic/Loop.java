package io.improbable.keanu.vertices.generic.nonprobabilistic;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public class Loop<T> {
    private final Vertex<T> start;

    public Loop(Vertex<T> start) {
        this.start = start;
    }

    public static <T> Loop startingFrom(Vertex<T> start) {
        return new Loop(start);
    }

    public LoopBodyBuilder apply(BayesianNetwork lambda) {
        return new LoopBodyBuilder(start, lambda);
    }

    public static class LoopBodyBuilder<T> {
        private final Vertex<Tensor<T>> start;
        private final BayesianNetwork lambda;

        public LoopBodyBuilder(Vertex start, BayesianNetwork lambda) {
            this.start = start;
            this.lambda = lambda;
        }

        public NumberLoopVertex<T> times(IntegerVertex times) {
            return new NumberLoopVertex<T>(start, lambda, times);
        }

        public WhileLoopVertex<T> whilst(BoolVertex condition) {
            return new WhileLoopVertex<T>(start, lambda, condition);
        }
    }
}
