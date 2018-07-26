package io.improbable.keanu.vertices.generic.nonprobabilistic;

import java.util.function.Function;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.operators.unary.IntegerUnaryOpLambda;

public class NumberLoopVertex<T> extends LoopVertex<T> {

    private final IntegerVertex times;
    private final Vertex<Tensor<T>> start;
    private final Vertex<Tensor<T>> lambda;
    private final Vertex<Tensor<T>> currentValue;

    public NumberLoopVertex(Vertex<Tensor<T>> start, Function<Tensor<T>,Tensor<T>> lambda, IntegerVertex times) {
        this.times = times;
        this.start = start;
        this.currentValue = ConstantVertex.of(start.getValue());
        this.lambda = new IntegerUnaryOpLambda(start.getShape(), currentValue, lambda);
        setParents(start, this.lambda, times);
    }

    @Override
    public Tensor<T> getDerivedValue() {
        return currentValue.getValue();
    }

    @Override
    public Tensor<T> sample(KeanuRandom random) {
        Tensor<T> value = start.getValue();
        IntegerTensor numTimes = times.sample();
        for (int i=0; i < numTimes.scalar().intValue(); i++) {
            currentValue.setValue(value);
            value = lambda.sample(random);
        }
        return value;
    }
}
