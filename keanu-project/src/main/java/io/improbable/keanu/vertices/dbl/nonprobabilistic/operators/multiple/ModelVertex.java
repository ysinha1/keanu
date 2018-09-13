package io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.multiple;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.vertices.NonProbabilistic;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexLabel;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.bool.nonprobabilistic.operators.BoolModelResultVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.DoubleModelResultVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.operators.IntegerModelResultVertex;

import java.util.Map;

public interface ModelVertex<T> extends NonProbabilistic<T> {

    void run(Map<VertexLabel, Vertex<? extends Tensor>> inputs);

    Map<VertexLabel, Object> updateValues(Map<VertexLabel, Vertex<? extends Tensor>> inputs);

    Double getDoubleModelOutputValue(VertexLabel label);

    Integer getIntegerModelOutputValue(VertexLabel label);

    Boolean getBooleanModelOutputValue(VertexLabel label);

    default DoubleVertex getDoubleModelOutputVertex(VertexLabel label) {
        return new DoubleModelResultVertex(this, label);
    }

    default IntegerVertex getIntegerModelOutputVertex(VertexLabel label) {
        return new IntegerModelResultVertex(this, label);
    }

    default BoolVertex getBoolModelOutputVertex(VertexLabel label) {
        return new BoolModelResultVertex(this, label);
    }

}
