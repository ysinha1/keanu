package io.improbable.keanu.vertices.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.VertexId;
import io.improbable.keanu.vertices.dbl.DoubleVertex;

public class MockVertexProvider {
    public DoubleVertex containing(double value) {
        return containing(DoubleTensor.scalar(value));
    }

    public DoubleVertex containing(DoubleTensor value) {
        DoubleVertex vertex = mock(DoubleVertex.class);
        when(vertex.getValue()).thenReturn(value);
        when(vertex.getShape()).thenReturn(value.getShape());
        when(vertex.getId()).thenReturn(new VertexId());
        return vertex;
    }
}
