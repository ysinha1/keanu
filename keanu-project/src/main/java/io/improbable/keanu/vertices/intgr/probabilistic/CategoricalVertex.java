package io.improbable.keanu.vertices.intgr.probabilistic;

import io.improbable.keanu.distributions.discrete.Categorical;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

import java.util.Map;

import static io.improbable.keanu.tensor.TensorShapeValidation.checkHasSingleNonScalarShapeOrAllScalar;
import static io.improbable.keanu.tensor.TensorShapeValidation.checkTensorsMatchNonScalarShapeOrAreScalar;

public class CategoricalVertex extends ProbabilisticInteger {

    private final IntegerVertex k;
    private final DoubleVertex p;

    public CategoricalVertex(int[] shape, IntegerVertex k, DoubleVertex p) {

        checkTensorsMatchNonScalarShapeOrAreScalar(shape, k.getShape(), p.getShape());

        this.k = k;
        this.p = p;
        setParents(k, p);
        setValue(IntegerTensor.placeHolder(shape));
    }

    public CategoricalVertex(IntegerVertex k, DoubleVertex p) {
        this(checkHasSingleNonScalarShapeOrAllScalar(k.getShape(), p.getShape()), k, p);
    }

    public CategoricalVertex(int k, double p) {
        this(ConstantVertex.of(k), ConstantVertex.of(p));
    }

    @Override
    public double logPmf(IntegerTensor value) {
        return 0;
    }

    @Override
    public Map<Long, DoubleTensor> dLogPmf(IntegerTensor value) {
        return null;
    }

    @Override
    public IntegerTensor sample(KeanuRandom random) {
        return Categorical.sample(k.getValue(), p.getValue(), random);
    }
}
