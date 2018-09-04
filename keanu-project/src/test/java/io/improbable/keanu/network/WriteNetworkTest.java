package io.improbable.keanu.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;

import io.improbable.keanu.network.write.KeanuCsvNetworkWriter;
import io.improbable.keanu.network.write.KeanuNetworkWriter;
import io.improbable.keanu.vertices.Vertex;

@RunWith(MockitoJUnitRunner.class)
public class WriteNetworkTest {
    BayesianNetwork network;

    @Mock
    private OutputStream outputStream;
    private Set<Vertex> vertices;

    @Before
    public void createNetwork() {
        Vertex parent1 = mock(Vertex.class);
        Vertex parent2 = mock(Vertex.class);
        Vertex child1 = mock(Vertex.class);
        Vertex child2 = mock(Vertex.class);
        Vertex grandchild1 = mock(Vertex.class);
        Vertex grandchild2 = mock(Vertex.class);
        when(parent1.getChildren()).thenReturn(ImmutableSet.of(child1, child2));
        when(parent2.getChildren()).thenReturn(ImmutableSet.of(child1, child2));
        when(child1.getChildren()).thenReturn(ImmutableSet.of(grandchild1, grandchild2));
        when(child2.getChildren()).thenReturn(ImmutableSet.of(grandchild1, grandchild2));
        when(child1.getParents()).thenReturn(ImmutableSet.of(parent1, parent2));
        when(child2.getParents()).thenReturn(ImmutableSet.of(parent1, parent2));
        when(grandchild1.getParents()).thenReturn(ImmutableSet.of(child1, child2));
        when(grandchild2.getParents()).thenReturn(ImmutableSet.of(child1, child2));

        when(parent1.getConnectedGraph()).thenCallRealMethod();

        vertices = parent1.getConnectedGraph();
        assertThat(vertices, hasSize(6));
        network = new BayesianNetwork(vertices);
    }

    @Test
    public void theCsvWriterWritesEveryVertex() throws IOException {
        KeanuNetworkWriter networkWriter = new KeanuCsvNetworkWriter("?");
        network.write(outputStream, networkWriter);
        for (Vertex v : vertices) {
            verify(v).getId();
            verify(v).getClass();
            verify(v).getParents();
        }
    }

    @Test
    public void youCanAddASeparatorWhenYouWriteTheNetworkAsCsv() throws IOException {
        String separator = "\n";
        KeanuNetworkWriter networkWriter = new KeanuCsvNetworkWriter(separator);
        network.write(outputStream, networkWriter);
        verify(outputStream, times(vertices.size() - 1)).write(separator.getBytes());
    }
}
