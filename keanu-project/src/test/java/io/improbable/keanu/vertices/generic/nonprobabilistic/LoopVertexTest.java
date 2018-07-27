package io.improbable.keanu.vertices.generic.nonprobabilistic;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public class LoopVertexTest {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Before
    public void setSeed() {
        KeanuRandom.setDefaultRandomSeed(0);
    }

    @Test
    public void youCanLoopNTimes() {
        log.info("***** Setting up");
        DoubleVertex start = ConstantVertex.of(0.);
        BayesianNetwork lambda = buildIncrementNetwork();
        IntegerVertex times = ConstantVertex.of(4);
        NumberLoopVertex<Double> loop = Loop.startingFrom(start).apply(lambda).times(times);
        log.info("***** getValue");
        assertEquals(4., loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("***** Sample");
        assertEquals(4., loop.sample().scalar().doubleValue(), 1e-8);
        log.info("***** setAndCascade");
        times.setAndCascade(6);
        log.info("***** getValue");
        assertEquals(6., loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("***** Sample");
        assertEquals(6., loop.sample().scalar().doubleValue(), 1e-8);
    }

    @Test
    public void youCanChangeTheNumberOfTimesToLoop() {
        log.info("***** Setting up");
        DoubleVertex start = ConstantVertex.of(0.);
        BayesianNetwork lambda = buildIncrementNetwork();
        IntegerVertex times = ConstantVertex.of(4);
        NumberLoopVertex<Double> loop = Loop.startingFrom(start).apply(lambda).times(times);
        log.info("***** setAndCascade");
        times.setAndCascade(6);
        log.info("***** getValue");
        assertEquals(6., loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("***** Sample");
        assertEquals(6., loop.sample().scalar().doubleValue(), 1e-8);
    }

    @Test
    public void youCanLoopWhileAConditionIsMet() {
        log.info("***** Setting up ******");
        DoubleVertex start = ConstantVertex.of(0.);
        BayesianNetwork lambda = buildIncrementNetwork();
        BoolVertex condition = new Flip(0.5);

        LoopVertex<Double> loop = Loop.startingFrom(start).apply(lambda).whilst(condition);
        log.info("****** Sample");
        assertEquals(5., loop.sample().scalar().doubleValue(), 1e-8);
        log.info("****** getValue");
        assertEquals(5., loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("****** observe");
        condition.observe(false);
        log.info("****** Sample");
        assertEquals(0., loop.sample().scalar().doubleValue(), 1e-8);
        log.info("****** getValue");
        assertEquals(0., loop.getValue().scalar().doubleValue(), 1e-8);
    }

    private BayesianNetwork buildIncrementNetwork() {
        PlaceholderVertex input = new PlaceholderVertex(1, 1);;
        Vertex output = ConstantVertex.of(1.).plus(input);
        return new BayesianNetwork(output.getConnectedGraph());
    }

    private BayesianNetwork buildLessThanConstantNetwork() {
        PlaceholderVertex input = new PlaceholderVertex(1, 1);;
        BoolVertex output = ConstantVertex.of(5).greaterThan(input);
        return new BayesianNetwork(output.getConnectedGraph());
    }

    @Test
    public void theConditionCanBeDependentOnTheLambda() {
        log.info("Setting up");
        DoubleVertex start = ConstantVertex.of(0.);
        BayesianNetwork lambda = buildIncrementNetwork();
        BayesianNetwork condition = buildLessThanConstantNetwork();

        LoopVertex<Double> loop = Loop.startingFrom(start).apply(lambda).whilst(condition);
        log.info("Sample");
        assertEquals(5, loop.sample().scalar().doubleValue(), 1e-8);
        log.info("getValue");
        assertEquals(5, loop.getValue().scalar().doubleValue(), 1e-8);
    }
}
