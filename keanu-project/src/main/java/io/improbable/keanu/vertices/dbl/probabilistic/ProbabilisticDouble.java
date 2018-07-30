package io.improbable.keanu.vertices.dbl.probabilistic;

import java.util.Map;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

public abstract class ProbabilisticDouble extends DoubleVertex {

    @Override
    public DoubleTensor updateValue() {
        if (!hasValue()) {
            setValue(sample());
        }
        return getValue();
    }

    @Override
    public boolean isProbabilistic() {
        return true;
    }

    @Override
    public DualNumber calculateDualNumber(Map<Differentiable, DualNumber> dualNumbers) {
        if (isObserved()) {
            return DualNumber.createConstant(getValue());
        } else {
            return DualNumber.createWithRespectToSelf(getId(), getValue());
        }
    }

}
