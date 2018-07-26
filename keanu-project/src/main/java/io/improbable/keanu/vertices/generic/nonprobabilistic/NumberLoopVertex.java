package io.improbable.keanu.vertices.generic.nonprobabilistic;

import com.google.common.collect.Iterables;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public class NumberLoopVertex<T> extends LoopVertex<T> {

    private final IntegerVertex times;
    private final Vertex<Tensor<T>> start;
    private final BayesianNetwork lambda;
    private final Vertex<Tensor<T>> input;
    private final Vertex<Tensor<T>> output;

    public NumberLoopVertex(Vertex<Tensor<T>> start, BayesianNetwork lambda, IntegerVertex times) {
        this.times = times;
        this.start = start;
        this.lambda = lambda;
        setParents(start, times);

        input = Iterables.getOnlyElement(lambda.getInputVertices());
        output = Iterables.getOnlyElement(lambda.getOutputVertices());

        input.setAndCascade(start.getValue());
    }

    @Override
    public Tensor<T> getDerivedValue() {
        return output.getValue();
    }

    @Override
    public Tensor<T> sample(KeanuRandom random) {
        Tensor<T> value = start.getValue();
        IntegerTensor numTimes = times.sample();
        for (int i=0; i < numTimes.scalar().intValue(); i++) {
            input.setAndCascade(value);
            value = output.sample(random);
        }
        return value;
    }
}
