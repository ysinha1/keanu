package io.improbable.keanu.vertices.generic.nonprobabilistic;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

public class WhileLoopVertex<T> extends LoopVertex<T> {

    private final Vertex<Tensor<T>> start;
    private final PlaceholderVertex input;
    private final Vertex<Tensor<T>> output;
    private final PlaceholderVertex conditionInput;
    private final BoolVertex conditionOutput;

    public WhileLoopVertex(Vertex<Tensor<T>> start, BayesianNetwork lambda, BoolVertex condition) {
        this(start, lambda, convertToNetwork(condition));
    }

    @NotNull
    private static BayesianNetwork convertToNetwork(BoolVertex condition) {
        PlaceholderVertex placeholder = new PlaceholderVertex();
        condition.addParent(placeholder);
        return new BayesianNetwork(ImmutableSet.of(condition, placeholder));
    }

    public WhileLoopVertex(Vertex<Tensor<T>> start, BayesianNetwork lambda, BayesianNetwork condition) {
        this.start = start;
        setParents(start);

        input = Iterables.getOnlyElement(lambda.getInputVertices());
        output = Iterables.getOnlyElement(lambda.getOutputVertices());
        conditionInput = Iterables.getOnlyElement(condition.getInputVertices());
        conditionOutput = (BoolVertex) Iterables.getOnlyElement(condition.getOutputVertices());

        input.setAndCascade((DoubleTensor) start.getValue());
    }

    @Override
    public Tensor<T> getDerivedValue() {
        return output.getValue();
    }

    @Override
    public Tensor<T> sample(KeanuRandom random) {
        Tensor<T> value = start.getValue();
        conditionInput.setAndCascade((DoubleTensor) value);
        while(conditionOutput.sample().scalar()) {
            input.setAndCascade((DoubleTensor) value);
            value = output.sample(random);
            conditionInput.setAndCascade((DoubleTensor) value);
        }
        return value;
    }

    @Override
    public DualNumber calculateDualNumber(Map<Differentiable, DualNumber> dualNumbers) {
        return ((Differentiable) output).calculateDualNumber(dualNumbers);
    }
}
