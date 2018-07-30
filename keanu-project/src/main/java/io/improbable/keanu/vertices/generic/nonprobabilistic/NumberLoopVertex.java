package io.improbable.keanu.vertices.generic.nonprobabilistic;

import java.util.Map;

import com.google.common.collect.Iterables;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public class NumberLoopVertex<T> extends LoopVertex<T> {

    private final IntegerVertex times;
    private final Vertex<Tensor<T>> start;
    private final PlaceholderVertex input;
    private final Vertex<Tensor<T>> output;

    public NumberLoopVertex(Vertex<Tensor<T>> start, BayesianNetwork lambda, IntegerVertex times) {
        this.times = times;
        this.start = start;
        setParents(times);

        input = Iterables.getOnlyElement(lambda.getInputVertices());
        output = Iterables.getOnlyElement(lambda.getOutputVertices());

        input.setAndCascade((DoubleTensor) start.getValue());
    }

    @Override
    public Tensor<T> getDerivedValue() {
        Tensor<T> value = start.getValue();
        IntegerTensor numTimes = times.sample();
        for (int i=0; i < numTimes.scalar().intValue(); i++) {
            input.setAndCascade((DoubleTensor) value);
            value = output.getValue();
        }
        return value;
    }

    @Override
    public Tensor<T> sample(KeanuRandom random) {
        Tensor<T> value = start.getValue();
        IntegerTensor numTimes = times.sample();
        for (int i=0; i < numTimes.scalar().intValue(); i++) {
            input.setAndCascade((DoubleTensor) value);
            value = output.sample(random);
        }
        return value;
    }

    @Override
    public DualNumber calculateDualNumber(Map<Differentiable, DualNumber> dualNumbers) {
        DualNumber value = ((Differentiable) start).getDualNumber();
        input.setDualNumber(value);
        IntegerTensor numTimes = times.sample();
        for (int i=0; i < numTimes.scalar().intValue(); i++) {
            value = ((Differentiable) output).getDualNumber();
            input.setDualNumber(value);
        }
        return value;
    }
}
