package io.improbable.keanu.vertices.generic.nonprobabilistic;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.bool.probabilistic.Flip;
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
        log.info("Setting up");
        IntegerVertex times = ConstantVertex.of(4);
        IntegerVertex start = ConstantVertex.of(0);
        BayesianNetwork lambda = buildIncrementNetwork();
        NumberLoopVertex<Integer> loop = Loop.startingFrom(start).apply(lambda).times(times);
        log.info("Sample");
        assertEquals(4, loop.sample().scalar().intValue());
        log.info("getValue");
        assertEquals(4, loop.getValue().scalar().intValue());
        log.info("setAndCascade");
        times.setAndCascade(42);
        log.info("Sample");
        assertEquals(42, loop.sample().scalar().intValue());
        log.info("getValue");
        assertEquals(42, loop.getValue().scalar().intValue());
    }

    @Test
    public void youCanLoopWhileAConditionIsMet() {
        log.info("Setting up");
        BoolVertex condition = new Flip(0.5);
        IntegerVertex start = ConstantVertex.of(0);
        BayesianNetwork lambda = buildIncrementNetwork();

        LoopVertex<Integer> loop = Loop.startingFrom(start).apply(lambda).whilst(condition);
        log.info("Sample");
        assertEquals(5, loop.sample().scalar().intValue());
        log.info("getValue");
        assertEquals(5, loop.getValue().scalar().intValue());
        log.info("setAndCascade");
        condition.observe(false);
        log.info("Sample");
        assertEquals(0, loop.sample().scalar().intValue());
        log.info("getValue");
        assertEquals(0, loop.getValue().scalar().intValue());
    }

    private BayesianNetwork buildIncrementNetwork() {
        PlaceholderVertex input = new PlaceholderVertex(1, 1);;
        IntegerVertex output = ConstantVertex.of(1).plus(input);
        return new BayesianNetwork(output.getConnectedGraph());
    }

    @Test
    public void theConditionCanBeDependentOnTheLambda() {
//        log.info("Setting up");
//        Function<IntegerTensor, BooleanTensor> condition = t -> t.lessThan(5);
//        IntegerVertex start = ConstantVertex.of(0);
//        BayesianNetwork lambda = buildIncrementNetwork();
//
//        LoopVertex<Integer> loop = Loop.startingFrom(start).apply(lambda).whilst(condition);
//        log.info("Sample");
//        assertEquals(5, loop.sample().scalar().intValue());
//        log.info("getValue");
//        assertEquals(5, loop.getValue().scalar().intValue());
//        log.info("setAndCascade");
//        condition.observe(false);
//        log.info("Sample");
//        assertEquals(0, loop.sample().scalar().intValue());
//        log.info("getValue");
//        assertEquals(0, loop.getValue().scalar().intValue());
    }
}
