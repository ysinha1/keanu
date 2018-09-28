package io.improbable.keanu.algorithms.statistics;

import io.improbable.keanu.tensor.TensorShapeValidation;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;

public class SummaryStatistics {
    private int[] shape = null;
    private int count;
    private DoubleTensor sum;
    private DoubleTensor sumOfSquares;

    public void addValue(DoubleTensor value) {
        if (this.shape == null) {
            initialise(value.getShape());
        }
        checkShapesMatch(value.getShape());
        sum = sum.plusInPlace(value);
        sumOfSquares = sumOfSquares.plusInPlace(value.times(value));
        count++;

    }

    private void checkShapesMatch(int[] shape) {
        TensorShapeValidation.checkAllShapesMatch(shape, this.shape);
    }

    private void initialise(int[] shape) {
        this.shape = shape;
        sum = DoubleTensor.zeros(shape);
        sumOfSquares = DoubleTensor.zeros(shape);
    }

    public IntegerTensor getCount() {
        return IntegerTensor.create(count, shape);
    }

    public DoubleTensor getMean() {
        return sum.div(count);
    }

    public DoubleTensor getVariance() {
        DoubleTensor meanSquared = getMean().pow(2);
        DoubleTensor secondMoment = sumOfSquares.div(count);
        return secondMoment.minus(meanSquared).times(count).div(count - 1);
    }

    public DoubleTensor getStandardDeviation() {
        return getVariance().sqrtInPlace();
    }
}
