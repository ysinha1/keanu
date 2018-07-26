package io.improbable.keanu.vertices.generic.nonprobabilistic;

import java.util.function.Function;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.operators.unary.IntegerUnaryOpLambda;

public class WhileLoopVertex<T> extends LoopVertex<T> {

    private final BoolVertex condition;
    private final Vertex<Tensor<T>> start;
    private final Vertex<Tensor<T>> lambda;
    private final Vertex<Tensor<T>> currentValue;

    public WhileLoopVertex(Vertex<Tensor<T>> start, Function<Tensor<T>,Tensor<T>> lambda, BoolVertex condition) {
        this.condition = condition;
        this.start = start;
        this.currentValue = ConstantVertex.of(start.getValue());
        this.lambda = new IntegerUnaryOpLambda(start.getShape(), currentValue, lambda);
        setParents(start, this.lambda, this.condition);
    }

    @Override
    public Tensor<T> getDerivedValue() {
 // can't loop here... if condition.getValue() were true we would loop forever
        return currentValue.getValue();
    }

    @Override
    public Tensor<T> sample(KeanuRandom random) {
        Tensor<T> value = start.getValue();
        while(condition.sample().scalar()) {
            value = lambda.sample(random);
            currentValue.setValue(value);
        }
        return value;
    }
}
