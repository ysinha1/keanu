package io.improbable.keanu.vertices.generic.nonprobabilistic;

import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.dbl.KeanuRandom;

public class PlaceholderVertex extends NonProbabilistic<IntegerTensor> {

    public PlaceholderVertex(int... shape) {
        setValue(IntegerTensor.placeHolder(shape));
    }

    @Override
    public IntegerTensor getDerivedValue() {
        return getValue();
    }

    @Override
    public IntegerTensor sample(KeanuRandom random) {
        return getValue();
    }
}
