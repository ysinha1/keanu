package io.improbable.keanu.vertices.dbl.nonprobabilistic;

import java.util.Collections;
import java.util.Map;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

public class ConstantDoubleVertex extends NonProbabilisticDouble {

    public ConstantDoubleVertex(DoubleTensor constant) {
        setValue(constant);
    }

    public ConstantDoubleVertex(double constant) {
        this(DoubleTensor.scalar(constant));
    }

    public ConstantDoubleVertex(double[] vector) {
        this(DoubleTensor.create(vector));
    }

    @Override
    public DualNumber calculateDualNumber(Map<Differentiable, DualNumber> dualNumbers) {
        return new DualNumber(getValue(), Collections.emptyMap());
    }

    @Override
    public DoubleTensor sample(KeanuRandom random) {
        return getValue();
    }

    @Override
    public DoubleTensor getDerivedValue() {
        return getValue();
    }

}
