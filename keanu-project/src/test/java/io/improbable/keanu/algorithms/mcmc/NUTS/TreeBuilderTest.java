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

    private DoubleVertex vertex;
    private VertexId vertexId;

    private List<Vertex<DoubleTensor>> vertices;
    private List<VertexId> ids;

    private BayesianNetwork bayesianNetwork;

    private KeanuRandom random = new KeanuRandom(1);
    private LogProbGradientCalculator mockedGradientCalculator;

    @Before
    public void setupTree() {
        random = new KeanuRandom(1);
        vertex = new GaussianVertex(0, 5.);
        vertices = Arrays.asList(vertex);
        bayesianNetwork = new BayesianNetwork(vertex.getConnectedGraph());

        vertexId = vertex.getId();
        ids = Arrays.asList(vertexId);

        Map<VertexId, DoubleTensor> mockedGradient = new HashMap<>();
        mockedGradient.put(vertexId, DoubleTensor.scalar(1.0));

        mockedGradientCalculator = mock(LogProbGradientCalculator.class);
        when(mockedGradientCalculator.getJointLogProbGradientWrtLatents()).thenAnswer(
            invocation -> mockedGradient
        );

        Map<VertexId, DoubleTensor> mockedReverseGradient = new HashMap<>();
        mockedReverseGradient.put(vertexId, DoubleTensor.scalar(-1.0));

        mockedGradientCalculator = mock(LogProbGradientCalculator.class);
        when(mockedGradientCalculator.getJointLogProbGradientWrtLatents()).thenAnswer(
            invocation -> mockedGradient
        );
    }

    @Test
    public void takesCorrectLeapfrogDistanceWithMockedGradient() {
        TreeBuilder tree = createStartingTree(5., 0.5);
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

        Assert.assertEquals(5.25, otherHalfOfTree.leapForward.position.get(vertexId).scalar(), 1e-6);
        Assert.assertEquals(1.0, otherHalfOfTree.leapForward.momentum.get(vertexId).scalar(), 1e-6);
        Assert.assertEquals(1.0, otherHalfOfTree.leapForward.gradient.get(vertexId).scalar(), 1e-6);

        assertMapsAreEqual(otherHalfOfTree.leapForward.position, otherHalfOfTree.leapBackward.position);
        assertMapsAreEqual(otherHalfOfTree.leapForward.gradient, otherHalfOfTree.leapBackward.gradient);
        assertMapsAreEqual(otherHalfOfTree.leapForward.momentum, otherHalfOfTree.leapBackward.momentum);

        assertMapsAreEqual(tree.leapForward.position, otherHalfOfTree.leapBackward.position);
        assertMapsAreEqual(tree.leapForward.gradient, otherHalfOfTree.leapBackward.gradient);
        assertMapsAreEqual(tree.leapForward.momentum, otherHalfOfTree.leapBackward.momentum);
    }

    @Test
    public void logProbDecreasesWhenMovingAwayFromCentreOfGaussian() {
        TreeBuilder tree = createStartingTree(5., 0.5);
        int treeHeight = 0;
        double epsilon = 1.;
        double logOfMasterPMinusMomentumBeforeLeapfrog = tree.logOfMasterPAtAcceptedPosition - tree.leapForward.halfDotProductMomentum();
        double u = random.nextDouble() * Math.exp(logOfMasterPMinusMomentumBeforeLeapfrog);
        LogProbGradientCalculator gradientCalculator = new LogProbGradientCalculator(vertices, vertices);

        TreeBuilder otherHalfOfTree = tree.buildOtherHalfOfTree(
            tree,
            vertices,
            bayesianNetwork.getLatentVertices(),
            gradientCalculator,
            vertices,
            u,
            1,
            treeHeight,
            epsilon,
            logOfMasterPMinusMomentumBeforeLeapfrog,
            random
        );

        Assert.assertTrue(otherHalfOfTree.logOfMasterPAtAcceptedPosition < tree.logOfMasterPAtAcceptedPosition);
        Assert.assertTrue(otherHalfOfTree.acceptedPosition.get(vertexId).scalar() > tree.acceptedPosition.get(vertexId).scalar());
    }

    @Test
    public void logProbIncreasesWhenMovingTowardsCentreOfGaussian() {
        TreeBuilder tree = createStartingTree(5., 0.5);
        int treeHeight = 0;
        double epsilon = 1.;
        double logOfMasterPMinusMomentumBeforeLeapfrog = tree.logOfMasterPAtAcceptedPosition - tree.leapForward.halfDotProductMomentum();
        double u = random.nextDouble() * Math.exp(logOfMasterPMinusMomentumBeforeLeapfrog);
        LogProbGradientCalculator gradientCalculator = new LogProbGradientCalculator(vertices, vertices);

        TreeBuilder otherHalfOfTree = tree.buildOtherHalfOfTree(
            tree,
            vertices,
            bayesianNetwork.getLatentVertices(),
            gradientCalculator,
            vertices,
            u,
            -1,
            treeHeight,
            epsilon,
            logOfMasterPMinusMomentumBeforeLeapfrog,
            random
        );

        Assert.assertTrue(otherHalfOfTree.logOfMasterPAtAcceptedPosition > tree.logOfMasterPAtAcceptedPosition);
        Assert.assertTrue(otherHalfOfTree.acceptedPosition.get(vertexId).scalar() < tree.acceptedPosition.get(vertexId).scalar());
    }


    @Test
    public void treeSizeTwo() {
        TreeBuilder tree = createStartingTree(5., 0.5);
        int treeHeight = 1;
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

    private TreeBuilder createStartingTree(double startingPosition, double startingMomentum) {
        Map<VertexId, DoubleTensor> position = fillMap(DoubleTensor.scalar(startingPosition));
        Map<VertexId, DoubleTensor> momentum = fillMap(DoubleTensor.scalar(startingMomentum));
        Map<VertexId, DoubleTensor> gradient = fillMap(DoubleTensor.scalar(0.0));

        Leapfrog leapForward = new Leapfrog(position, momentum, gradient);
        Leapfrog leapBackward = new Leapfrog(position, momentum, gradient);

        vertex.setValue(startingPosition);
        double initialLogOfMasterP = ProbabilityCalculator.calculateLogProbFor(vertices);

        return new TreeBuilder(
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


    private Map<VertexId, DoubleTensor> fillMap(DoubleTensor value) {
        Map<VertexId, DoubleTensor> map = new HashMap<>();
        for (VertexId id : ids) {
            map.put(id, value);
        }
        return map;
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
