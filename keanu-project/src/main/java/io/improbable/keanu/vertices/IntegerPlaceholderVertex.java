package io.improbable.keanu.vertices;

import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public class IntegerPlaceholderVertex extends IntegerVertex implements LogProbGraph.PlaceholderVertex, NonProbabilistic<IntegerTensor>, Differentiable, NonSaveableVertex {

    private final IntegerVertex defaultVertex;

    public IntegerPlaceholderVertex(long... initialShape) {
        super(initialShape);
        defaultVertex = null;
    }

    public IntegerPlaceholderVertex(IntegerVertex defaultVertex) {
        super(defaultVertex.getShape());
        this.defaultVertex = defaultVertex;
    }

    @Override
    public IntegerTensor calculate() {
        if (hasValue()) {
            return getValue();
        } else if (defaultVertex != null) {
            return defaultVertex.getValue();
        } else {
            throw new IllegalStateException("Placeholders must be fed values");
        }
    }

}
