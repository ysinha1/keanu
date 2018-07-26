package io.improbable.keanu.vertices.generic.nonprobabilistic;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.improbable.keanu.tensor.intgr.IntegerTensor;
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
//        InputVertex x = new InputVertex(start);
        Function<IntegerTensor, IntegerTensor> lambda = t -> t.plus(1);
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
        Function<IntegerTensor, IntegerTensor> lambda = t -> t.plus(1);
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
}
