package io.improbable.keanu.vertices.bool.probabilistic;

import io.improbable.keanu.distributions.discrete.Bernoulli;
import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.TensorShape;
import io.improbable.keanu.tensor.bool.BooleanTensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.PartialDerivatives;
import io.improbable.keanu.vertices.update.ProbabilisticValueUpdater;

import java.util.Map;

import static io.improbable.keanu.tensor.TensorShapeValidation.checkTensorsMatchNonScalarShapeOrAreScalar;

public class BernoulliVertex extends BoolVertex implements ProbabilisticBoolean {

    private final Vertex<DoubleTensor> probTrue;

    /**
     * One probTrue that must match a proposed tensor shape of Bernoulli.
     * <p>
     * If all provided parameters are scalar then the proposed shape determines the shape
     *
     * @param shape    the desired shape of the vertex
     * @param probTrue the probability the bernoulli returns true
     */
    public BernoulliVertex(int[] shape, Vertex<DoubleTensor> probTrue) {
        super(new ProbabilisticValueUpdater<>());
        checkTensorsMatchNonScalarShapeOrAreScalar(shape, probTrue.getShape());
        this.probTrue = probTrue;
        setParents(probTrue);
        setValue(BooleanTensor.placeHolder(shape));
    }

    /**
     * One to one constructor for mapping some shape of probTrue to
     * a matching shaped Bernoulli.
     *
     * @param probTrue probTrue with same shape as desired Bernoulli tensor or scalar
     */
    public BernoulliVertex(Vertex<DoubleTensor> probTrue) {
        this(probTrue.getShape(), probTrue);
    }

    public BernoulliVertex(double probTrue) {
        this(Tensor.SCALAR_SHAPE, new ConstantDoubleVertex(probTrue));
    }

    public BernoulliVertex(int[] shape, double probTrue) {
        this(shape, new ConstantDoubleVertex(probTrue));
    }

    public Vertex<DoubleTensor> getProbTrue() {
        return probTrue;
    }

    @Override
    public double logProb(BooleanTensor value) {
        return Bernoulli.withParameters(probTrue.getValue()).logProb(value).sum();
    }

    @Override
    public Map<Long, DoubleTensor> dLogProb(BooleanTensor value) {

        if (!(probTrue instanceof Differentiable)) {
            throw new UnsupportedOperationException("Probability is non-differentiable");
        }

        DualNumber probTrueDual = ((Differentiable) probTrue).getDualNumber();
        DoubleTensor probTrueValue = probTrueDual.getValue();
        PartialDerivatives probTruePartialDerivatives = probTrueDual.getPartialDerivatives();

        DoubleTensor greaterThanMask = probTrueValue
            .getGreaterThanMask(DoubleTensor.ONE_SCALAR);

        DoubleTensor lessThanOrEqualToMask = probTrueValue
            .getLessThanOrEqualToMask(DoubleTensor.ZERO_SCALAR);

        DoubleTensor greaterThanOneOrLessThanZero = greaterThanMask.plusInPlace(lessThanOrEqualToMask);

        DoubleTensor dlogProbdxForTrue = probTrueValue.reciprocal();
        dlogProbdxForTrue = dlogProbdxForTrue.setWithMaskInPlace(greaterThanOneOrLessThanZero, 0.0);

        DoubleTensor dlogProbdxForFalse = probTrueValue.minus(1.0).reciprocalInPlace();
        dlogProbdxForFalse = dlogProbdxForFalse.setWithMaskInPlace(greaterThanOneOrLessThanZero, 0.0);

        DoubleTensor dLogPdp = value.setDoubleIf(
            dlogProbdxForTrue,
            dlogProbdxForFalse
        );

        PartialDerivatives partials = probTruePartialDerivatives
            .multiplyBy(dLogPdp)
            .sum(true, TensorShape.dimensionRange(0, value.getRank()));

        return partials.asMap();
    }

    @Override
    public BooleanTensor sample(KeanuRandom random) {
        return Bernoulli.withParameters(probTrue.getValue()).sample(this.getShape(), random);
    }
}
