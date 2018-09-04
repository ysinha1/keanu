package io.improbable.keanu.network;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

import io.improbable.keanu.network.write.KeanuCsvNetworkWriter;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

@RunWith(MockitoJUnitRunner.class)
public class KeanuCsvNetworkWriterTest {

    @Mock
    private OutputStream outputStream;

    @Test
    public void youCanSpecifyTheSeparator() throws IOException {
        String separator = "?";
        KeanuCsvNetworkWriter networkWriter = new KeanuCsvNetworkWriter(separator);
        IntegerVertex v1 = ConstantVertex.of(0);
        DoubleVertex v2 = new CastDoubleVertex(v1);
        List<? extends Vertex> vertices = ImmutableList.of(v1, v2);
        networkWriter.write(outputStream, vertices);
        verify(outputStream).write(separator.getBytes());
    }
}
