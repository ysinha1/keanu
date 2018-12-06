package io.improbable.keanu.algorithms.mcmc.NUTS;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MCMCTestDistributions;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.testcategory.Slow;
import io.improbable.keanu.vertices.ProbabilityCalculator;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexId;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.LogProbGradientCalculator;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.vis.Vizer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class NUTSTest {

    private KeanuRandom random;

    @Before
    public void setup() {
        random = new KeanuRandom(3);
    }

    @Category(Slow.class)
    @Test
    public void samplesGaussian() {
        double mu = 0.0;
        double sigma = 1.0;
        BayesianNetwork simpleGaussian = MCMCTestDistributions.createSimpleGaussian(mu, sigma, 0.1, random);

        NUTS nuts = NUTS.builder()
            .adaptEnabled(false)
            .initialStepSize(0.5)
            .random(random)
            .maxTreeHeight(8)
            .build();

        NetworkSamples posteriorSamples = nuts.getPosteriorSamples(
            simpleGaussian,
            simpleGaussian.getLatentVertices(),
            10000
        );


        Vertex<DoubleTensor> vertex = simpleGaussian.getContinuousLatentVertices().get(0);

//        MCMCTestDistributions.samplesMatchSimpleGaussian(mu, sigma, posteriorSamples.get(vertex).asList(), 0.1);

        Vizer.histogram(posteriorSamples.getDoubleTensorSamples(vertex.getId()).asList());
        while (true) {
        }
    }

    @Test
    public void samplesContinuousPrior() {

        BayesianNetwork bayesNet = MCMCTestDistributions.createSumOfGaussianDistribution(20.0, 1.0, 46., 15.0);

        int sampleCount = 6000;
        NUTS nuts = NUTS.builder()
            .adaptCount(sampleCount)
            .maxTreeHeight(4)
            .random(random)
            .build();

        NetworkSamples posteriorSamples = nuts.getPosteriorSamples(
            bayesNet,
            bayesNet.getLatentVertices(),
            sampleCount
        ).drop((int) (sampleCount * 0.25));

        Vertex<DoubleTensor> A = bayesNet.getContinuousLatentVertices().get(0);
        Vertex<DoubleTensor> B = bayesNet.getContinuousLatentVertices().get(1);

        MCMCTestDistributions.samplesMatchesSumOfGaussians(44.0, posteriorSamples.get(A).asList(), posteriorSamples.get(B).asList());
    }

    @Category(Slow.class)
    @Test
    public void samplesFromDonut() {
        BayesianNetwork donutBayesNet = MCMCTestDistributions.create2DDonutDistribution();

        NUTS nuts = NUTS.builder()
            .adaptCount(1000)
            .random(random)
            .build();

        NetworkSamples samples = nuts.getPosteriorSamples(
            donutBayesNet,
            donutBayesNet.getLatentVertices(),
            1000
        );

        Vertex<DoubleTensor> A = donutBayesNet.getContinuousLatentVertices().get(0);
        Vertex<DoubleTensor> B = donutBayesNet.getContinuousLatentVertices().get(1);

        MCMCTestDistributions.samplesMatch2DDonut(samples.get(A).asList(), samples.get(B).asList());
    }

    @Test
    public void canReduceStepsizeFromLargeInitialToSmallToExploreSmallSpace() {
        double startingStepsize = 10.;

        NUTSSampler.AutoTune tune = new NUTSSampler.AutoTune(
            startingStepsize,
            0.65,
            50
        );

        TreeBuilder treeLessLikely = TreeBuilder.createBasicTree(Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_MAP, 0., Collections.EMPTY_MAP);
        treeLessLikely.deltaLikelihoodOfLeapfrog = -50.;
        treeLessLikely.treeSize = 8.;

        double adaptedStepSizeLessLikely = NUTSSampler.adaptStepSize(tune, treeLessLikely, 1);

        Assert.assertTrue(adaptedStepSizeLessLikely < startingStepsize);

        TreeBuilder treeMoreLikely = TreeBuilder.createBasicTree(Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_MAP, 0., Collections.EMPTY_MAP);
        treeMoreLikely.deltaLikelihoodOfLeapfrog = 50.;
        treeMoreLikely.treeSize = 8.;

        double adaptedStepSizeMoreLikely = NUTSSampler.adaptStepSize(tune, treeMoreLikely, 1);

        Assert.assertTrue(adaptedStepSizeMoreLikely > startingStepsize);
    }

    @Test
    public void canFindSmallStartingStepsizeForSmallSpace() {
        DoubleVertex vertex = new GaussianVertex(0, 0.05);
        List<DoubleVertex> vertices = Arrays.asList(vertex);
        BayesianNetwork bayesianNetwork = new BayesianNetwork(vertex.getConnectedGraph());

        VertexId vertexId = vertex.getId();

        LogProbGradientCalculator logProbGradientCalculator = new LogProbGradientCalculator(bayesianNetwork.getLatentOrObservedVertices(), vertices);
        vertex.setValue(DoubleTensor.scalar(1.));
        Map<VertexId, DoubleTensor> position = Collections.singletonMap(vertexId, vertex.getValue());
        Map<VertexId, DoubleTensor> gradient = logProbGradientCalculator.getJointLogProbGradientWrtLatents();

        double startingStepsize = NUTSSampler.findStartingStepSize(
            position,
            gradient,
            Arrays.asList(vertex),
            bayesianNetwork.getLatentVertices(),
            logProbGradientCalculator,
            ProbabilityCalculator.calculateLogProbFor(vertices),
            random
        );

        double startingEpsilon = 1.0;
        Assert.assertTrue(startingStepsize < startingEpsilon);
    }

    @Test
    public void canFindLargeStartingStepsizeForLargeSpace() {
        DoubleVertex vertex = new GaussianVertex(0, 500.);
        List<DoubleVertex> vertices = Arrays.asList(vertex);
        BayesianNetwork bayesianNetwork = new BayesianNetwork(vertex.getConnectedGraph());

        VertexId vertexId = vertex.getId();

        LogProbGradientCalculator logProbGradientCalculator = new LogProbGradientCalculator(bayesianNetwork.getLatentOrObservedVertices(), vertices);
        Map<VertexId, DoubleTensor> position = Collections.singletonMap(vertexId, vertex.sample(random));
        Map<VertexId, DoubleTensor> gradient = logProbGradientCalculator.getJointLogProbGradientWrtLatents();

        double startingStepsize = NUTSSampler.findStartingStepSize(
            position,
            gradient,
            Arrays.asList(vertex),
            bayesianNetwork.getLatentVertices(),
            logProbGradientCalculator,
            ProbabilityCalculator.calculateLogProbFor(vertices),
            random
        );

        Assert.assertTrue(startingStepsize > 64);
    }

    @Test
    public void canDefaultToSettingsInBuilderAndIsConfigurableAfterBuilding() {

        GaussianVertex A = new GaussianVertex(0.0, 1.0);
        BayesianNetwork net = new BayesianNetwork(A.getConnectedGraph());
        net.probeForNonZeroProbability(100, random);

        NUTS nuts = NUTS.builder()
            .build();

        assertTrue(nuts.getAdaptCount() > 0);
        assertTrue(nuts.getTargetAcceptanceProb() > 0);
        assertNotNull(nuts.getRandom());

        NetworkSamples posteriorSamples = nuts.getPosteriorSamples(
            net,
            net.getLatentVertices(),
            2
        );

        nuts.setRandom(null);
        assertNull(nuts.getRandom());

        assertFalse(posteriorSamples.get(A).asList().isEmpty());
    }
}
