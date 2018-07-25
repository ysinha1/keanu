package io.improbable.keanu.vertices.generic.nonprobabilistic;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public class LoopVertex<T> extends NonProbabilistic<Tensor<T>> {

    private final IntegerVertex times;
    private final Vertex<? extends Tensor<T>> start;
    private final Vertex<? extends Tensor<T>> lambda;

    // package private
    LoopVertex(int[] shape, IntegerVertex times, Vertex<? extends Tensor<T>> start, Vertex<? extends Tensor<T>> lambda)  {
        this.times = times;
        this.start = start;
        this.lambda = lambda;
        setParents(times, start, lambda);
        setValue(Tensor.placeHolder(shape));
    }

    @Override
    public Tensor<T> getDerivedValue() {
        return lambda.getValue();
    }

    @Override
    public Tensor<T> sample(KeanuRandom random) {
        return null;
    }
}
