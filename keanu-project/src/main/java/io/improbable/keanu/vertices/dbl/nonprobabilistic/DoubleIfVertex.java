package io.improbable.keanu.vertices.dbl.nonprobabilistic;

import io.improbable.keanu.tensor.TensorShape;
import io.improbable.keanu.tensor.bool.BooleanTensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

import java.util.Map;

public class DoubleIfVertex extends NonProbabilisticDouble {

    private final Vertex<? extends BooleanTensor> predicate;
    private final Vertex<? extends DoubleTensor> thn;
    private final Vertex<? extends DoubleTensor> els;

    public DoubleIfVertex(int[] shape,
                          Vertex<? extends BooleanTensor> predicate,
                          Vertex<? extends DoubleTensor> thn,
                          Vertex<? extends DoubleTensor> els) {

        this.predicate = predicate;
        this.thn = thn;
        this.els = els;
        setParents(predicate, thn, els);
        setValue(DoubleTensor.placeHolder(shape));
    }

    @Override
    public DoubleTensor sample(KeanuRandom random) {
        return op(predicate.sample(random), thn.sample(random), els.sample(random));
    }

    @Override
    public DoubleTensor getDerivedValue() {
        return op(predicate.getValue(), thn.getValue(), els.getValue());
    }

    private DoubleTensor op(BooleanTensor predicate, DoubleTensor thn, DoubleTensor els) {
        return predicate.setDoubleIf(thn, els);
    }

    @Override
    protected DualNumber calculateDualNumber(Map<Vertex, DualNumber> dualNumbers) {
        BooleanTensor predicateValue = predicate.getValue();
        if (predicateValue.isScalar()) {
            if (predicateValue.scalar()) {
                return new DualNumber(dualNumbers.get(thn).getValue(), dualNumbers.get(thn).getPartialDerivatives());
            } else {
                return new DualNumber(dualNumbers.get(els).getValue(), dualNumbers.get(els).getPartialDerivatives());
            }
        } else {
            double[] flatBools = predicateValue.asFlatDoubleArray();
            for (int i = 0; i < flatBools.length; i++) {
                if (flatBools[i] == 1.0) {

                } else {

                }
            }
        }
        return null;
    }
}
