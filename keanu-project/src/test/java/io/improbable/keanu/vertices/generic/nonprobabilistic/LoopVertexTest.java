package io.improbable.keanu.vertices.generic.nonprobabilistic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.operators.unary.IntegerUnaryOpLambda;

public class LoopVertexTest {

    @Test
    public void youCanLoopNTimes() {
        IntegerVertex times = ConstantVertex.of(42);
        IntegerVertex start = ConstantVertex.of(0);
        InputVertex x = new InputVertex(start);
        IntegerVertex lambda = new IntegerUnaryOpLambda<IntegerTensor>(new int[] {1}, x, t -> t.plus(1));
        LoopVertex<Integer> loop = Loop.times(times).startingFrom(start).apply(lambda);
        assertEquals(42, loop.getValue().scalar().intValue());
        times.setAndCascade(4);
        assertEquals(4, loop.getValue());
    }
}
