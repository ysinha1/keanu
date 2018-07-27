package io.improbable.keanu.vertices.generic.nonprobabilistic;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.KeanuRandom;

public class PlaceholderVertex extends NonProbabilistic<DoubleTensor> {

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
}
