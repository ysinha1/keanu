package io.improbable.keanu.backend.keanu.compiled;

import com.google.common.collect.ImmutableList;
import io.improbable.keanu.algorithms.VariableReference;
import io.improbable.keanu.backend.ComputableGraph;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.UniformIntVertex;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class KeanuCompiledGraphTest {

    @Test
    public void compilesEmptyGraph() {

        KeanuCompiledGraphBuilder compiler = new KeanuCompiledGraphBuilder();

        ComputableGraph computableGraph = compiler.build();

        Map<VariableReference, ?> result = computableGraph.compute(Collections.emptyMap(), Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    public void compilesAddition() {
        assertBinaryDoubleMatches(DoubleVertex::plus);
    }

    @Test
    public void compilesSubtraction() {
        assertBinaryDoubleMatches(DoubleVertex::minus);
    }

    @Test
    public void compilesMultiplication() {
        assertBinaryDoubleMatches(DoubleVertex::times);
    }

    @Test
    public void compilesDivision() {
        assertBinaryDoubleMatches(DoubleVertex::div);
    }

    @Test
    public void compilesATan2() {
        assertBinaryDoubleMatches(DoubleVertex::atan2);
    }

    @Test
    public void compilesPow() {
        assertBinaryDoubleMatches(DoubleVertex::pow);
    }

    @Test
    public void compilesMatrixMultiply() {
        assertBinaryDoubleMatches(new long[]{2, 3}, new long[]{3, 2}, DoubleVertex::matrixMultiply);
    }

    @Test
    public void compilesSeveralChainedOpsWithConstant() {
        assertBinaryDoubleMatches((a, b) -> a.plus(b).times(b).div(2).minus(a));
    }

    private void assertBinaryDoubleMatches(BiFunction<DoubleVertex, DoubleVertex, DoubleVertex> op) {
        assertBinaryDoubleMatches(new long[0], new long[0], op);
    }

    private void assertBinaryDoubleMatches(long[] shapeA, long[] shapeB, BiFunction<DoubleVertex, DoubleVertex, DoubleVertex> op) {
        KeanuCompiledGraphBuilder compiler = new KeanuCompiledGraphBuilder();

        GaussianVertex A = new GaussianVertex(shapeA, 0, 1);
        GaussianVertex B = new GaussianVertex(shapeB, 0, 1);

        DoubleVertex C = op.apply(A, B);

        compiler.convert(C.getConnectedGraph(), ImmutableList.of(C));

        ComputableGraph computableGraph = compiler.build();

        Map<VariableReference, Object> inputs = new HashMap<>();
        inputs.put(A.getReference(), A.getValue());
        inputs.put(B.getReference(), B.getValue());

        Map<VariableReference, ?> result = computableGraph.compute(inputs, Collections.emptyList());

        assertEquals(C.getValue(), result.get(C.getReference()));
    }

    @Test
    public void canAddDirectlyToGraph() {
        KeanuCompiledGraphBuilder compiler = new KeanuCompiledGraphBuilder();

        GaussianVertex A = new GaussianVertex(0, 1);
        GaussianVertex B = new GaussianVertex(0, 1);

        DoubleVertex C = A.times(B);

        compiler.convert(C.getConnectedGraph(), ImmutableList.of(C));

        VariableReference summation = compiler.add(A.getReference(), C.getReference());
        compiler.registerOutput(summation);

        ComputableGraph computableGraph = compiler.build();

        Map<VariableReference, Object> inputs = new HashMap<>();
        inputs.put(A.getReference(), A.getValue());
        inputs.put(B.getReference(), B.getValue());

        Map<VariableReference, ?> result = computableGraph.compute(inputs, Collections.emptyList());

        assertEquals(C.getValue(), result.get(C.getReference()));
        assertEquals(C.getValue().plus(A.getValue()), result.get(summation));
    }

    @Test
    public void canReshapeDouble() {
        assertUnaryDoubleMatches(new long[]{3, 4}, (a) -> a.reshape(6, 2));
    }

    @Test
    public void compilesSum() {
        assertUnaryDoubleMatches(new long[]{2, 2}, DoubleVertex::sum);
        assertUnaryDoubleMatches(new long[]{2, 2}, (a) -> a.sum(0));
        assertUnaryDoubleMatches(new long[]{2, 2}, (a) -> a.sum(1));
    }

    @Test
    public void compilesSimpleUnaryOps() {
        assertUnaryDoubleMatches(DoubleVertex::abs);
        assertUnaryDoubleMatches(DoubleVertex::cos);
        assertUnaryDoubleMatches(DoubleVertex::acos);
        assertUnaryDoubleMatches(DoubleVertex::exp);
        assertUnaryDoubleMatches(DoubleVertex::log);
        assertUnaryDoubleMatches(DoubleVertex::logGamma);
        assertUnaryDoubleMatches(DoubleVertex::sin);
        assertUnaryDoubleMatches(DoubleVertex::asin);
        assertUnaryDoubleMatches(DoubleVertex::tan);
        assertUnaryDoubleMatches(DoubleVertex::atan);
        assertUnaryDoubleMatches(DoubleVertex::ceil);
        assertUnaryDoubleMatches(DoubleVertex::floor);
        assertUnaryDoubleMatches(DoubleVertex::round);
        assertUnaryDoubleMatches(DoubleVertex::sigmoid);
    }

    @Test
    public void compilesSquareMatrices() {
        assertUnaryDoubleMatches(new long[]{2, 2}, DoubleVertex::matrixDeterminant);
        assertUnaryDoubleMatches(new long[]{2, 2}, DoubleVertex::matrixInverse);
    }

    private void assertUnaryDoubleMatches(Function<DoubleVertex, DoubleVertex> op) {
        assertUnaryDoubleMatches(new long[0], op);
        assertUnaryDoubleMatches(new long[]{2}, op);
        assertUnaryDoubleMatches(new long[]{2, 2}, op);
    }

    private void assertUnaryDoubleMatches(long[] shape, Function<DoubleVertex, DoubleVertex> op) {
        KeanuCompiledGraphBuilder compiler = new KeanuCompiledGraphBuilder();

        UniformVertex A = new UniformVertex(shape, 0, 1);

        DoubleVertex C = op.apply(A);

        compiler.convert(C.getConnectedGraph(), ImmutableList.of(C));

        ComputableGraph computableGraph = compiler.build();

        Map<VariableReference, Object> inputs = new HashMap<>();
        inputs.put(A.getReference(), A.getValue());

        Map<VariableReference, ?> result = computableGraph.compute(inputs, Collections.emptyList());

        assertEquals(C.getValue(), result.get(C.getReference()));
    }

    @Test
    public void canReshapeInteger() {
        assertUnaryIntegerMatches(new long[]{3, 4}, (a) -> a.reshape(6, 2));
    }

    @Test
    public void compilesSimpleUnaryIntegerOps() {
        assertUnaryIntegerMatches(IntegerVertex::abs);
    }

    private void assertUnaryIntegerMatches(Function<IntegerVertex, IntegerVertex> op) {
        assertUnaryIntegerMatches(new long[0], op);
    }

    private void assertUnaryIntegerMatches(long[] shape, Function<IntegerVertex, IntegerVertex> op) {
        KeanuCompiledGraphBuilder compiler = new KeanuCompiledGraphBuilder();

        UniformIntVertex A = new UniformIntVertex(shape, 0, 1);

        IntegerVertex C = op.apply(A);

        compiler.convert(C.getConnectedGraph(), ImmutableList.of(C));

        ComputableGraph computableGraph = compiler.build();

        Map<VariableReference, Object> inputs = new HashMap<>();
        inputs.put(A.getReference(), A.getValue());

        Map<VariableReference, ?> result = computableGraph.compute(inputs, Collections.emptyList());

        assertEquals(C.getValue(), result.get(C.getReference()));
    }

}
