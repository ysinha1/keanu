package io.improbable.keanu.benchmarks;

import com.google.common.primitives.Ints;
import io.improbable.keanu.tensor.bool.BooleanTensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.dbl.Nd4jDoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.List;
import java.util.function.Function;

@State(Scope.Benchmark)
public class Nd4jVsJVM {

    private static final double OPERAND = 1.00001;
    private static final int NUM_OPERATIONS = 10;

    private Nd4jDoubleTensor startingND4J;
    private Nd4jDoubleTensor ND4JOperand;
    private JVMTensor startingJVM;
    private JVMTensor JVMOperand;
    private long[] tensorShape;

    @Param({"10", "100", "1000"})
    public long matrixDimensions;

    @Setup
    public void setUp() {
        tensorShape = new long[] {matrixDimensions, matrixDimensions};
        startingND4J = Nd4jDoubleTensor.create(OPERAND, tensorShape);
        ND4JOperand = Nd4jDoubleTensor.create(OPERAND, tensorShape);
        startingJVM = new JVMTensor(OPERAND, tensorShape);
        JVMOperand = new JVMTensor(OPERAND, tensorShape);
    }

    @Benchmark
    public double nd4jAddition() {
        DoubleTensor result = startingND4J;

        result = result.plusInPlace(ND4JOperand);

        return result.scalar();
    }

    @Benchmark
    public double nativeJVMAddition() {
        DoubleTensor result = startingJVM;

        result = result.plusInPlace(JVMOperand);

        return result.scalar();
    }

    @Benchmark
    public double nativeMatrixMultiply() {
        DoubleTensor result = startingJVM;

        result = result.matrixMultiply(JVMOperand);

        return result.scalar();
    }

    @Benchmark
    public double nd4jMatrixMultiply() {
        DoubleTensor result = startingND4J;

        result = result.matrixMultiply(ND4JOperand);

        return result.scalar();
    }

    private class JVMTensor implements DoubleTensor {

        private final double[] storage;

        JVMTensor(long[] shape) {
            storage = new double[getStorageLength(shape)];
        }

        JVMTensor(double value, long[] shape) {
            storage = new double[getStorageLength(shape)];

            for (int i = 0; i < storage.length; i++) {
                storage[i] = value;
            }
        }

        private final int getStorageLength(long[] shape) {
            long length = shape[0];

            for (int i = 1; i < shape.length; i++) {
                length *= shape[i];
            }

            return Ints.checkedCast(length);
        }

        @Override
        public DoubleTensor setValue(Double value, long... index) {
            return null;
        }

        @Override
        public DoubleTensor reshape(long... newShape) {
            return null;
        }

        @Override
        public DoubleTensor permute(int... rearrange) {
            return null;
        }

        @Override
        public DoubleTensor duplicate() {
            return null;
        }

        @Override
        public DoubleTensor diag() {
            return null;
        }

        @Override
        public DoubleTensor transpose() {
            return null;
        }

        @Override
        public DoubleTensor sum(int... overDimensions) {
            return null;
        }

        @Override
        public DoubleTensor reciprocal() {
            return null;
        }

        @Override
        public DoubleTensor minus(double value) {
            return null;
        }

        @Override
        public DoubleTensor plus(double value) {
            return null;
        }

        @Override
        public DoubleTensor times(double value) {
            return null;
        }

        @Override
        public DoubleTensor div(double value) {
            return null;
        }

        @Override
        public DoubleTensor matrixMultiply(DoubleTensor value) {
            JVMTensor castedThat = (JVMTensor)value;
            JVMTensor resultTensor = new JVMTensor(tensorShape);

            for (int i = 0; i < matrixDimensions; i++) {
                for (int j = 0; j < matrixDimensions; j++) {
                    double total = 0;

                    for (int k = 0; k < matrixDimensions; k++) {
                        double currentResult = this.storage[convertRowColumnToIndex(i, k)]
                            * castedThat.storage[convertRowColumnToIndex(k, j)];
                        total += currentResult;
                    }

                    resultTensor.storage[convertRowColumnToIndex(i, j)] = total;
                }
            }

            return resultTensor;
        }

        private int convertRowColumnToIndex(int row, int column) {
            return Ints.checkedCast(row * matrixDimensions + column);
        }

        @Override
        public DoubleTensor tensorMultiply(DoubleTensor value, int[] dimsLeft, int[] dimsRight) {
            return null;
        }

        @Override
        public DoubleTensor pow(DoubleTensor exponent) {
            return null;
        }

        @Override
        public DoubleTensor pow(double exponent) {
            return null;
        }

        @Override
        public DoubleTensor sqrt() {
            return null;
        }

        @Override
        public DoubleTensor log() {
            return null;
        }

        @Override
        public DoubleTensor safeLogTimes(DoubleTensor y) {
            return null;
        }

        @Override
        public DoubleTensor logGamma() {
            return null;
        }

        @Override
        public DoubleTensor digamma() {
            return null;
        }

        @Override
        public DoubleTensor sin() {
            return null;
        }

        @Override
        public DoubleTensor cos() {
            return null;
        }

        @Override
        public DoubleTensor tan() {
            return null;
        }

        @Override
        public DoubleTensor atan() {
            return null;
        }

        @Override
        public DoubleTensor atan2(double y) {
            return null;
        }

        @Override
        public DoubleTensor atan2(DoubleTensor y) {
            return null;
        }

        @Override
        public DoubleTensor asin() {
            return null;
        }

        @Override
        public DoubleTensor acos() {
            return null;
        }

        @Override
        public DoubleTensor exp() {
            return null;
        }

        @Override
        public DoubleTensor matrixInverse() {
            return null;
        }

        @Override
        public double max() {
            return 0;
        }

        @Override
        public double min() {
            return 0;
        }

        @Override
        public double average() {
            return 0;
        }

        @Override
        public double standardDeviation() {
            return 0;
        }

        @Override
        public boolean equalsWithinEpsilon(DoubleTensor other, double epsilon) {
            return false;
        }

        @Override
        public DoubleTensor standardize() {
            return null;
        }

        @Override
        public DoubleTensor replaceNaN(double value) {
            return null;
        }

        @Override
        public DoubleTensor clamp(DoubleTensor min, DoubleTensor max) {
            return null;
        }

        @Override
        public DoubleTensor ceil() {
            return null;
        }

        @Override
        public DoubleTensor floor() {
            return null;
        }

        @Override
        public DoubleTensor round() {
            return null;
        }

        @Override
        public DoubleTensor sigmoid() {
            return null;
        }

        @Override
        public DoubleTensor choleskyDecomposition() {
            return null;
        }

        @Override
        public double determinant() {
            return 0;
        }

        @Override
        public double product() {
            return 0;
        }

        @Override
        public DoubleTensor slice(int dimension, long index) {
            return null;
        }

        @Override
        public List<DoubleTensor> split(int dimension, long... splitAtIndices) {
            return null;
        }

        @Override
        public DoubleTensor reciprocalInPlace() {
            return null;
        }

        @Override
        public DoubleTensor minusInPlace(double value) {
            return null;
        }

        @Override
        public DoubleTensor plusInPlace(double value) {
            return null;
        }

        @Override
        public DoubleTensor timesInPlace(double value) {
            return null;
        }

        @Override
        public DoubleTensor divInPlace(double value) {
            return null;
        }

        @Override
        public DoubleTensor powInPlace(double exponent) {
            return null;
        }

        @Override
        public DoubleTensor sqrtInPlace() {
            return null;
        }

        @Override
        public DoubleTensor logInPlace() {
            return null;
        }

        @Override
        public DoubleTensor safeLogTimesInPlace(DoubleTensor y) {
            return null;
        }

        @Override
        public DoubleTensor logGammaInPlace() {
            return null;
        }

        @Override
        public DoubleTensor digammaInPlace() {
            return null;
        }

        @Override
        public DoubleTensor sinInPlace() {
            return null;
        }

        @Override
        public DoubleTensor cosInPlace() {
            return null;
        }

        @Override
        public DoubleTensor tanInPlace() {
            return null;
        }

        @Override
        public DoubleTensor atanInPlace() {
            return null;
        }

        @Override
        public DoubleTensor atan2InPlace(double y) {
            return null;
        }

        @Override
        public DoubleTensor atan2InPlace(DoubleTensor y) {
            return null;
        }

        @Override
        public DoubleTensor asinInPlace() {
            return null;
        }

        @Override
        public DoubleTensor acosInPlace() {
            return null;
        }

        @Override
        public DoubleTensor expInPlace() {
            return null;
        }

        @Override
        public DoubleTensor minInPlace(DoubleTensor min) {
            return null;
        }

        @Override
        public DoubleTensor maxInPlace(DoubleTensor max) {
            return null;
        }

        @Override
        public DoubleTensor clampInPlace(DoubleTensor min, DoubleTensor max) {
            return null;
        }

        @Override
        public DoubleTensor ceilInPlace() {
            return null;
        }

        @Override
        public DoubleTensor floorInPlace() {
            return null;
        }

        @Override
        public DoubleTensor roundInPlace() {
            return null;
        }

        @Override
        public DoubleTensor sigmoidInPlace() {
            return null;
        }

        @Override
        public DoubleTensor standardizeInPlace() {
            return null;
        }

        @Override
        public DoubleTensor replaceNaNInPlace(double value) {
            return null;
        }

        @Override
        public DoubleTensor setAllInPlace(double value) {
            return null;
        }

        @Override
        public BooleanTensor lessThan(double value) {
            return null;
        }

        @Override
        public BooleanTensor lessThanOrEqual(double value) {
            return null;
        }

        @Override
        public BooleanTensor greaterThan(double value) {
            return null;
        }

        @Override
        public BooleanTensor greaterThanOrEqual(double value) {
            return null;
        }

        @Override
        public BooleanTensor notNaN() {
            return null;
        }

        @Override
        public Double sum() {
            return null;
        }

        @Override
        public DoubleTensor toDouble() {
            return null;
        }

        @Override
        public IntegerTensor toInteger() {
            return null;
        }

        @Override
        public DoubleTensor abs() {
            return null;
        }

        @Override
        public int argMax() {
            return 0;
        }

        @Override
        public IntegerTensor argMax(int axis) {
            return null;
        }

        @Override
        public DoubleTensor getGreaterThanMask(DoubleTensor greaterThanThis) {
            return null;
        }

        @Override
        public DoubleTensor getGreaterThanOrEqualToMask(DoubleTensor greaterThanThis) {
            return null;
        }

        @Override
        public DoubleTensor getLessThanMask(DoubleTensor lessThanThis) {
            return null;
        }

        @Override
        public DoubleTensor getLessThanOrEqualToMask(DoubleTensor lessThanThis) {
            return null;
        }

        @Override
        public DoubleTensor setWithMaskInPlace(DoubleTensor mask, Double value) {
            return null;
        }

        @Override
        public DoubleTensor setWithMask(DoubleTensor mask, Double value) {
            return null;
        }

        @Override
        public DoubleTensor apply(Function<Double, Double> function) {
            return null;
        }

        @Override
        public DoubleTensor minusInPlace(DoubleTensor that) {
            return null;
        }

        @Override
        public DoubleTensor plusInPlace(DoubleTensor that) {
            JVMTensor castedThat = (JVMTensor)that;

            if (this.storage.length != castedThat.storage.length) {
                throw new IllegalArgumentException("Tensor length doesn't match");
            }

            for (int i = 0; i < this.storage.length; i++) {
                storage[i] += castedThat.storage[i];
            }

            return this;
        }

        @Override
        public DoubleTensor timesInPlace(DoubleTensor that) {
            return null;
        }

        @Override
        public DoubleTensor divInPlace(DoubleTensor that) {
            return null;
        }

        @Override
        public DoubleTensor powInPlace(DoubleTensor exponent) {
            return null;
        }

        @Override
        public DoubleTensor unaryMinusInPlace() {
            return null;
        }

        @Override
        public DoubleTensor absInPlace() {
            return null;
        }

        @Override
        public DoubleTensor applyInPlace(Function<Double, Double> function) {
            return null;
        }

        @Override
        public BooleanTensor lessThan(DoubleTensor value) {
            return null;
        }

        @Override
        public BooleanTensor lessThanOrEqual(DoubleTensor value) {
            return null;
        }

        @Override
        public BooleanTensor greaterThan(DoubleTensor value) {
            return null;
        }

        @Override
        public BooleanTensor greaterThanOrEqual(DoubleTensor value) {
            return null;
        }

        @Override
        public DoubleTensor minus(DoubleTensor that) {
            return null;
        }

        @Override
        public DoubleTensor plus(DoubleTensor that) {
            return null;
        }

        @Override
        public DoubleTensor times(DoubleTensor that) {
            return null;
        }

        @Override
        public DoubleTensor div(DoubleTensor that) {
            return null;
        }

        @Override
        public DoubleTensor unaryMinus() {
            return null;
        }

        @Override
        public int getRank() {
            return 0;
        }

        @Override
        public long[] getShape() {
            return new long[0];
        }

        @Override
        public long getLength() {
            return 0;
        }

        @Override
        public boolean isShapePlaceholder() {
            return false;
        }

        @Override
        public Double getValue(long... index) {
            return null;
        }

        @Override
        public Double scalar() {
            return storage[0];
        }

        @Override
        public double[] asFlatDoubleArray() {
            return new double[0];
        }

        @Override
        public int[] asFlatIntegerArray() {
            return new int[0];
        }

        @Override
        public Double[] asFlatArray() {
            return new Double[0];
        }

        @Override
        public FlattenedView<Double> getFlattenedView() {
            return null;
        }

        @Override
        public BooleanTensor elementwiseEquals(Double value) {
            return null;
        }
    }
}
