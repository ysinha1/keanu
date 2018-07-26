package io.improbable.keanu.vertices.generic.nonprobabilistic;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;

public class InputVertex<T> extends NonProbabilistic<Tensor<T>> {
    private final Vertex<? extends Tensor<T>> start;

    public InputVertex(Vertex<? extends Tensor<T>> start) {
        this.start = start;
        setParents(start);
        setValue(Tensor.placeHolder(start.getShape()));
    }

    @Override
    public Tensor<T> getDerivedValue() {
        return start.getValue();
    }

    @Override
    public Tensor<T> sample(KeanuRandom random) {
        return start.sample(random);
    }
}
