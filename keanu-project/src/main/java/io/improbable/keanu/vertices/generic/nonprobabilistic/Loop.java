package io.improbable.keanu.vertices.generic.nonprobabilistic;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public class Loop {
    private final IntegerVertex times;

    private Loop(IntegerVertex times) {
        this.times = times;
    }

    public LoopBodyBuilder startingFrom(IntegerVertex start) {
        return new LoopBodyBuilder(times, start);
    }

    public static Loop times(IntegerVertex times) {
        return new Loop(times);
    }
    public static class LoopBodyBuilder<T> {

        private final IntegerVertex times;
        private final Vertex<? extends Tensor<T>> start;

        public LoopBodyBuilder(IntegerVertex times, Vertex<? extends Tensor<T>> start) {

            this.times = times;
            this.start = start;
        }

        public LoopVertex apply(Vertex<? extends Tensor<T>> lambda) {
            return new LoopVertex<T>(lambda.getShape(), times, start, lambda);
        }
    }
}
