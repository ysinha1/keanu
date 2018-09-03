package io.improbable.keanu.network;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexId;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;

@RunWith(MockitoJUnitRunner.class)
public class WriteVertexTest {

    @Mock
    private OutputStream outputStream;

    @Test
    public void youCanWriteAVertexType() throws IOException {
        Vertex vertex = ConstantVertex.of(0.0);
        vertex.write(System.out);
        vertex.write(outputStream);
        verify(outputStream).write("ConstantDoubleVertex".getBytes());
    }

    @Test
    public void youCanWriteAVertexWithItsParents() throws IOException {
        Vertex parent = mock(Vertex.class);
        when(parent.getId()).thenReturn(new VertexId());
        Vertex vertex = new CastDoubleVertex(parent);
        vertex.write(System.out);
        vertex.write(outputStream);
        verify(outputStream).write("2|CastDoubleVertex|[1]".getBytes());

    }
}
