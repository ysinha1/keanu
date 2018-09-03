package io.improbable.keanu.network;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import io.improbable.keanu.vertices.Vertex;

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
        when(child1.getParents()).thenReturn(ImmutableSet.of(parent1, parent2));
        Vertex child2 = mock(Vertex.class);
        when(child2.getParents()).thenReturn(ImmutableSet.of(parent1, parent2));
        Vertex grandchild1 = mock(Vertex.class);
        when(grandchild1.getParents()).thenReturn(ImmutableSet.of(child1, child2));
        Vertex grandchild2 = mock(Vertex.class);
        when(grandchild2.getParents()).thenReturn(ImmutableSet.of(child1, child2));

        vertices = parent1.getConnectedGraph();
        network = new BayesianNetwork(vertices);
    }

    @Test
    public void itWritesEveryVertex() {
        network.write(outputStream);
        for (Vertex v : vertices) {
            verify(v).write(outputStream);
        }
    }
}
