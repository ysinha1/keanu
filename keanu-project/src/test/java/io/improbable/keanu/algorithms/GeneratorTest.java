package io.improbable.keanu.algorithms;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.improbable.keanu.tensor.generic.GenericTensor;
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
}
