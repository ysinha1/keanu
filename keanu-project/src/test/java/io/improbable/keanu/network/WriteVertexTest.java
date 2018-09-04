package io.improbable.keanu.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

import io.improbable.keanu.network.write.KeanuCsvNetworkWriter;
import io.improbable.keanu.util.json.KeanuJsonWriter;
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
        VertexId.ID_GENERATOR.set(0);
    }

    @Test
    public void youCanWriteAVertexWithItsParentsAsCsv() throws IOException {
        Vertex parent = mock(Vertex.class);
        when(parent.getId()).thenReturn(new VertexId());
        Vertex vertex = new CastDoubleVertex(parent);
        KeanuCsvNetworkWriter networkWriter = new KeanuCsvNetworkWriter("|");
        networkWriter.write(outputStream, ImmutableList.of(vertex));
        verify(outputStream).write("1|CastDoubleVertex|[0]".getBytes());

    }

    @Test
    public void youCanWriteAVertexWithItsParentsAsJson() throws IOException {
        Vertex parent = ConstantVertex.of(0.1);
        Vertex vertex = new CastDoubleVertex(parent);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStream outStream = new PrintStream(byteArrayOutputStream, true, "UTF-8");
        KeanuJsonWriter jsonWriter = new KeanuJsonWriter();
        jsonWriter.writeValue(outStream, vertex);
        assertThat(byteArrayOutputStream.toString(), equalTo("{\"id\":\"[1]\",\"type\":\"CastDoubleVertex\",\"parents\":[\"[0]\"]}"));
    }

}
