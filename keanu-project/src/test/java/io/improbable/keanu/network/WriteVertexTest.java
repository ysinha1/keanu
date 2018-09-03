package io.improbable.keanu.network;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.Vertex;

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
}
