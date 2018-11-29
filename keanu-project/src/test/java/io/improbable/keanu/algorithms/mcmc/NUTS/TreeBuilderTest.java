package io.improbable.keanu.algorithms.mcmc.NUTS;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.ProbabilityCalculator;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexId;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.LogProbGradientCalculator;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;

public class TreeBuilderTest {

    private DoubleVertex A;
    private DoubleVertex B;

    private VertexId aID;
    private VertexId bID;

    private List<Vertex<DoubleTensor>> vertices;
    private List<VertexId> ids;

    private Map<VertexId, DoubleTensor> position;
    private Map<VertexId, DoubleTensor> momentum;
    private Map<VertexId, DoubleTensor> gradient;

    private BayesianNetwork bayesianNetwork;

    private KeanuRandom random;
    private LogProbGradientCalculator mockedGradientCalculator;
    private TreeBuilder tree;

    @Before
    public void setupTree() {
        random = new KeanuRandom(1);

        A = new GaussianVertex(0, 5.);
        B = new GaussianVertex(0, 5.);
        vertices = Arrays.asList(A, B);
        bayesianNetwork = new BayesianNetwork(A.getConnectedGraph());

        aID = A.getId();
        bID = B.getId();
        ids = Arrays.asList(aID, bID);

        position = new HashMap<>();
        momentum = new HashMap<>();
        gradient = new HashMap<>();

        fillMap(position, DoubleTensor.scalar(5.0));
        fillMap(momentum, DoubleTensor.scalar(0.5));
        fillMap(gradient, DoubleTensor.scalar(0.0));

        Leapfrog leapForward = new Leapfrog(position, momentum, gradient);
        Leapfrog leapBackward = new Leapfrog(position, momentum, gradient);

        double initialLogOfMasterP = ProbabilityCalculator.calculateLogProbFor(vertices);

        Map<VertexId, DoubleTensor> mockedGradient = new HashMap<>();
        mockedGradient.put(aID, DoubleTensor.scalar(1.0));
        mockedGradient.put(bID, DoubleTensor.scalar(1.0));

        mockedGradientCalculator = mock(LogProbGradientCalculator.class);
        when(mockedGradientCalculator.getJointLogProbGradientWrtLatents()).thenAnswer(
            invocation -> mockedGradient
        );

        tree = new TreeBuilder(
            leapForward,
            leapBackward,
            position,
            gradient,
            initialLogOfMasterP,
            takeSample(vertices),
            1,
            true,
            0,
            1
        );

    }

    @Test
    public void takesOneLeapfrogOnBasecase() {
        int treeHeight = 0;
        double epsilon = 1.;
        double logOfMasterPMinusMomentumBeforeLeapfrog = tree.logOfMasterPAtAcceptedPosition - tree.leapForward.halfDotProductMomentum();
        double u = random.nextDouble() * Math.exp(logOfMasterPMinusMomentumBeforeLeapfrog);

        TreeBuilder otherHalfOfTree = tree.buildOtherHalfOfTree(
            tree,
            vertices,
            bayesianNetwork.getLatentVertices(),
            mockedGradientCalculator,
            vertices,
            u,
            1,
            treeHeight,
            epsilon,
            logOfMasterPMinusMomentumBeforeLeapfrog,
            random
        );

        Assert.assertEquals(5.25, otherHalfOfTree.leapForward.position.get(aID).scalar(), 1e-6);
        Assert.assertEquals(5.25, otherHalfOfTree.leapForward.position.get(bID).scalar(), 1e-6);

        Assert.assertEquals(1.0, otherHalfOfTree.leapForward.momentum.get(aID).scalar(), 1e-6);
        Assert.assertEquals(1.0, otherHalfOfTree.leapForward.momentum.get(bID).scalar(), 1e-6);

        Assert.assertEquals(1.0, otherHalfOfTree.leapForward.gradient.get(aID).scalar(), 1e-6);
        Assert.assertEquals(1.0, otherHalfOfTree.leapForward.gradient.get(bID).scalar(), 1e-6);

        assertMapsAreEqual(otherHalfOfTree.leapForward.position, otherHalfOfTree.leapBackward.position);
        assertMapsAreEqual(otherHalfOfTree.leapForward.gradient, otherHalfOfTree.leapBackward.gradient);
        assertMapsAreEqual(otherHalfOfTree.leapForward.momentum, otherHalfOfTree.leapBackward.momentum);

        assertMapsAreEqual(tree.leapForward.position, otherHalfOfTree.leapBackward.position);
        assertMapsAreEqual(tree.leapForward.gradient, otherHalfOfTree.leapBackward.gradient);
        assertMapsAreEqual(tree.leapForward.momentum, otherHalfOfTree.leapBackward.momentum);
    }

    @Test
    public void treeSizeTwo() {
        int treeHeight = 2;
        double epsilon = 1.;
        double logOfMasterPMinusMomentumBeforeLeapfrog = tree.logOfMasterPAtAcceptedPosition - tree.leapForward.halfDotProductMomentum();
        double u = random.nextDouble() * Math.exp(logOfMasterPMinusMomentumBeforeLeapfrog);

        TreeBuilder otherHalfOfTree = tree.buildOtherHalfOfTree(
            tree,
            vertices,
            bayesianNetwork.getLatentVertices(),
            mockedGradientCalculator,
            vertices,
            u,
            1,
            treeHeight,
            epsilon,
            logOfMasterPMinusMomentumBeforeLeapfrog,
            random
        );

    }


        private void fillMap(Map<VertexId, DoubleTensor> map, DoubleTensor value) {
        for (VertexId id : ids) {
            map.put(id, value);
        }
    }

    private static Map<VertexId, ?> takeSample(List<? extends Vertex> sampleFromVertices) {
        Map<VertexId, ?> sample = new HashMap<>();
        for (Vertex vertex : sampleFromVertices) {
            putValue(vertex, sample);
        }
        return sample;
    }

    private static <T> void putValue(Vertex<T> vertex, Map<VertexId, ?> target) {
        ((Map<VertexId, T>) target).put(vertex.getId(), vertex.getValue());
    }

    private void assertMapsAreEqual(Map<VertexId, DoubleTensor> one, Map<VertexId, DoubleTensor> two) {
        for (VertexId id : one.keySet()) {
            Assert.assertEquals(one.get(id).scalar(), two.get(id).scalar(), 1e-6);
        }
    }
}
