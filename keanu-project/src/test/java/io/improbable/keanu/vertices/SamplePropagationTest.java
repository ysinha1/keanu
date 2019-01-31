package io.improbable.keanu.vertices;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import io.improbable.keanu.KeanuRandom;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.DoubleBinaryOpVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.ProbabilisticDouble;

public class SamplePropagationTest {

    @Test
    public void doesNotPerformUnnecessarySamples() {
        AtomicInteger sampleCount = new AtomicInteger(0);

        DoubleVertex top = ConstantVertex.of(5.0);

        DoubleVertex A = new PassThroughProbabilisticVertex(top, sampleCount, id -> System.out.println("Sample on id:" + id));
        DoubleVertex BShared = new PassThroughProbabilisticVertex(top, sampleCount, id -> System.out.println("Sample on id:" + id));
        DoubleVertex C = new PassThroughProbabilisticVertex(top, sampleCount, id -> System.out.println("Sample on id:" + id));

        DoubleVertex D = new PassThroughBinaryOpVertex(A, BShared, sampleCount, id -> System.out.println("Sample on id:" + id));
        DoubleVertex E = new PassThroughBinaryOpVertex(BShared, C, sampleCount, id -> System.out.println("Sample on id:" + id));

        DoubleVertex F = new PassThroughBinaryOpVertex(D, E, sampleCount, id -> System.out.println("Sample on id:" + id));

        F.sample();
        Assert.assertEquals(6, sampleCount.get());
    }


    public class PassThroughBinaryOpVertex extends DoubleBinaryOpVertex implements Differentiable, NonSaveableVertex {

        private final AtomicInteger sampleCount;
        private final Consumer<VertexId> onSample;

        public PassThroughBinaryOpVertex(DoubleVertex left, DoubleVertex right, AtomicInteger sampleCount, Consumer<VertexId> onSample) {
            super(left, right);
            this.sampleCount = sampleCount;
            this.onSample = onSample;
        }

        @Override
        protected DoubleTensor op(DoubleTensor l, DoubleTensor r) {
            return l;
        }

        @Override
        public DoubleTensor sample(KeanuRandom random) {
            sampleCount.incrementAndGet();
            onSample.accept(getId());
            return op(left.sample(random), right.sample(random));
        }

    }

    public class PassThroughProbabilisticVertex extends DoubleVertex implements Differentiable, ProbabilisticDouble, NonSaveableVertex {

        private final AtomicInteger sampleCount;
        private final Consumer<VertexId> onSample;
        private final DoubleVertex inputVertex;

        public PassThroughProbabilisticVertex(DoubleVertex inputVertex, AtomicInteger sampleCount, Consumer<VertexId> onSample) {
            super(inputVertex.getShape());
            this.sampleCount = sampleCount;
            this.onSample = onSample;
            this.inputVertex = inputVertex;
        }

        @Override
        public DoubleTensor sample(KeanuRandom random) {
            sampleCount.incrementAndGet();
            onSample.accept(getId());
            return inputVertex.sample(random);
        }

        @Override
        public double logProb(DoubleTensor value) {
            return 0;
        }

        @Override
        public Map<Vertex, DoubleTensor> dLogProb(DoubleTensor atValue, Set<? extends Vertex> withRespectTo) {
            return null;
        }
    }


}
