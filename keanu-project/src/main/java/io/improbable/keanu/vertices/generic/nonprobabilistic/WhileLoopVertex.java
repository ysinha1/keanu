package io.improbable.keanu.vertices.generic.nonprobabilistic;

import com.google.common.collect.Iterables;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;

public class WhileLoopVertex<T> extends LoopVertex<T> {

    private final BoolVertex condition;
    private final Vertex<Tensor<T>> start;
    private final BayesianNetwork lambda;
    private final Vertex<Tensor<T>> input;
    private final Vertex<Tensor<T>> output;

    public WhileLoopVertex(Vertex<Tensor<T>> start, BayesianNetwork lambda, BoolVertex condition) {
        this.condition = condition;
        this.start = start;
        this.lambda = lambda;
        setParents(start, this.condition);

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
        while(condition.sample().scalar()) {
            input.setAndCascade(value);
            value = output.sample(random);
        }
        return value;
    }
}
