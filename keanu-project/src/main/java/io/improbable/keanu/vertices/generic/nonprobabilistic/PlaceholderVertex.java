package io.improbable.keanu.vertices.generic.nonprobabilistic;

import java.util.Map;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

public class PlaceholderVertex extends NonProbabilistic<DoubleTensor> implements Differentiable {

    private DualNumber dualNumber;

    public PlaceholderVertex(int... shape) {
        setValue(DoubleTensor.placeHolder(shape));
    }

    @Override
    public DoubleTensor getDerivedValue() {
        return getValue();
    }

    @Override
    public DoubleTensor sample(KeanuRandom random) {
        return getValue();
    }

    public void setDualNumber(DualNumber dualNumber) {
        this.dualNumber = dualNumber;
    }

    @Override
    public DualNumber calculateDualNumber(Map<Differentiable, DualNumber> dualNumbers) {
        return dualNumber;
    }
}
