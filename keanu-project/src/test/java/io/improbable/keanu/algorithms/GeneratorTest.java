package io.improbable.keanu.algorithms;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.generic.GenericTensor;
import io.improbable.keanu.util.dot.DotSaver;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.generic.probabilistic.discrete.CategoricalVertex;

public class GeneratorTest {

    private KeanuRandom random;

    @Before
    public void setup() {
        random = new KeanuRandom(1);
    }

    @Test
    public void canInitialiseGenerator() {
        Generator g = new Generator();
        List<DoubleVertex> vertices = g.getVertices();
        Assert.assertTrue(vertices.size() == 1);
    }

    @Test
    public void canInitialiseCategoricalForVertexSelection() {
        Generator g = new Generator();
        CategoricalVertex<Generator.Vertices, GenericTensor<Generator.Vertices>> c = g.getCategoricalVertex();
        GenericTensor<Generator.Vertices> v = c.sample(random);
        Assert.assertTrue(v.scalar() instanceof Generator.Vertices);
    }

    @Test
    public void canAddAVertex() {
        Generator g = new Generator();
        List<DoubleVertex> vertices = g.getVertices();
        Assert.assertTrue(vertices.size() == 1);
        g.transition();
        vertices = g.getVertices();
        Assert.assertTrue(vertices.size() > 1);
    }

    @Test
    public void canAddManyVertices() {
        Generator g = new Generator();
        List<DoubleVertex> vertices = g.getVertices();
        Assert.assertTrue(vertices.size() == 1);
        g.transition(100);
        vertices = g.getVertices();
        Assert.assertTrue(vertices.size() > 1);
    }

    @Test
    public void canWriteToDotFile() throws IOException {
        Generator g = new Generator();
        g.transition(150);
        List<DoubleVertex> vertices = g.getVertices();
        DotSaver dotSaver = new DotSaver(new BayesianNetwork(vertices.get(0).getConnectedGraph()));
        OutputStream outputWriter = new FileOutputStream("net.dot");
        dotSaver.save(outputWriter, true);
    }
}
