package io.improbable.keanu.network;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Before;
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

    @Before
    public void resetVertexIds() {
        VertexId.ID_GENERATOR.set(0)x;
    }

    @Test
    public void youCanWriteAVertexWithItsParentsAsCsv() throws IOException {
        Vertex parent = mock(Vertex.class);
        when(parent.getId()).thenReturn(new VertexId());
        Vertex vertex = new CastDoubleVertex(parent);
        vertex.write(System.out);
        vertex.write(outputStream);
        verify(outputStream).write("1|CastDoubleVertex|[0]".getBytes());

    }

    @Test
    public void youCanWriteAVertexWithItsParentsAsJson() throws IOException {
        Vertex parent = ConstantVertex.of(0.1);
        Vertex vertex = new CastDoubleVertex(parent);
//        vertex.writeAsJson(System.out);
        vertex.writeAsJson(outputStream);
        verify(outputStream).write("1|CastDoubleVertex|[0]".getBytes());

    }
}
