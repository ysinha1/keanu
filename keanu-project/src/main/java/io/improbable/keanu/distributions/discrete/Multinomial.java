package io.improbable.keanu.distributions.discrete;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.nd4j.linalg.util.ArrayUtil;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.improbable.keanu.distributions.DiscreteDistribution;
import io.improbable.keanu.tensor.TensorShapeValidation;
import io.improbable.keanu.tensor.bool.BooleanTensor;
import io.improbable.keanu.tensor.bool.SimpleBooleanTensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.tensor.validate.DebugTensorValidator;
import io.improbable.keanu.tensor.validate.TensorValidator;
import io.improbable.keanu.vertices.dbl.KeanuRandom;


public class Multinomial implements DiscreteDistribution {

    public static final DebugTensorValidator CATEGORY_PROBABILITIES_CANNOT_BE_ZERO = new DebugTensorValidator<>(TensorValidator.ZERO_CATCHER);
    private final IntegerTensor n;
    private final DoubleTensor p;
    private final int numCategories;
    private final List<DoubleTensor> cumulativeProbabilities;

    public static Multinomial withParameters(IntegerTensor n, DoubleTensor p) {
        return new Multinomial(n, p);
    }

    /**
     * @param n The number of draws from the variable
     * @param p The probability of observing each of the k values (which sum to 1)
     *          p is a Tensor whose first dimension must be of size k
     * @see <a href="https://en.wikipedia.org/wiki/Multinomial_distribution">Multinomial Distribution</a>
     * Generalisation of the Binomial distribution to variables with more than 2 possible values
     */
    private Multinomial(IntegerTensor n, DoubleTensor p) {
        Preconditions.checkArgument(
            p.sum(0).elementwiseEquals(DoubleTensor.ones(n.getShape())).allTrue(),
            "Probabilities must sum to one"
        );
        CATEGORY_PROBABILITIES_CANNOT_BE_ZERO.validate(p);

        numCategories = p.getShape()[0];
        TensorShapeValidation.checkAllShapesMatch(n.getShape(), p.slice(0, 0).getShape());
        this.n = n;
        this.p = p;
        cumulativeProbabilities = calculateCumulativeProbabilities();
    }

    @Override
    public IntegerTensor sample(int[] shape, KeanuRandom random) {
        TensorShapeValidation.checkTensorsMatchNonScalarShapeOrAreScalar(shape, n.getShape());

        IntegerTensor counts = IntegerTensor.zeros(sampleShapeFor(shape));
        for (int i = 0; i < n.max(); i++) {
            BooleanTensor stillSampling = extrude(n.greaterThanOrEqual(i), sampleShapeFor(shape));
            DoubleTensor randomNumbers = random.nextDouble(shape);
            BooleanTensor sampleCategory = calculateCategoryToSampleFrom(randomNumbers);
            counts = sampleCategory.and(stillSampling).setIntegerIf(counts.plus(1), counts);
        }
        return counts;
    }

    private BooleanTensor extrude(BooleanTensor slice, int[] shape) {
        int times = ArrayUtil.prod(shape);
        int[] newShape = ArrayUtil.combine(slice.getShape(), shape);
        if (newShape[0] == 1) {
            newShape = ArrayUtils.remove(newShape, 0);
        }
        boolean[] sliceAsArray = ArrayUtils.toPrimitive(slice.asFlatArray());
        int sliceLength = sliceAsArray.length;
        boolean[] sliceRepeated = new boolean[sliceLength * times];
        for (int i = 0; i < times; i++) {
            int destPos = sliceLength * i;
            System.arraycopy(sliceAsArray, 0, sliceRepeated, destPos, sliceLength);
        }
        return new SimpleBooleanTensor(sliceRepeated, newShape);
    }

    private BooleanTensor calculateCategoryToSampleFrom(DoubleTensor randomNumbers) {
        List<DoubleTensor> extrudedCumulativeProbabilities = cumulativeProbabilities;
        if (n.isScalar()) {
            extrudedCumulativeProbabilities = cumulativeProbabilities.stream()
                .map(t -> DoubleTensor.create(t.scalar(), randomNumbers.getShape()))
                .collect(Collectors.toList());
        }
        ImmutableList.Builder<BooleanTensor> sampleFromCategories = ImmutableList.builder();
        BooleanTensor foundCategory = BooleanTensor.create(false, randomNumbers.getShape());
        BooleanTensor sampleFromCategory;
        for (DoubleTensor cumulativeProbability : extrudedCumulativeProbabilities) {
            sampleFromCategory = randomNumbers.lessThanOrEqual(cumulativeProbability)
                .andInPlace(foundCategory.not())
                .andInPlace(extrude(cumulativeProbability.greaterThan(0.), randomNumbers.getShape()));
            foundCategory.orInPlace(sampleFromCategory);
            sampleFromCategories.add(sampleFromCategory);
        }
        return BooleanTensor.concat(0, sampleFromCategories.build().toArray(new BooleanTensor[0])).reshape(sampleShapeFor(randomNumbers.getShape()));
    }

    private int[] sampleShapeFor(int[] shape) {
        int[] firstDimension = Arrays.copyOfRange(p.getShape(), 0, 1);
        int[] remainingDimensions = shape;
        if (shape[0] == 1) {
            remainingDimensions = Arrays.copyOfRange(shape, 1, shape.length);
        } else if (shape[shape.length - 1] == 1) {
            remainingDimensions = Arrays.copyOfRange(shape, 0, shape.length - 1);
        }
        return ArrayUtil.combine(firstDimension, remainingDimensions);
    }

    private List<DoubleTensor> calculateCumulativeProbabilities() {

        List<DoubleTensor> cumulativeProbabilities = Lists.newArrayList();
        DoubleTensor cumulativeProbability = DoubleTensor.zeros(n.getShape());
        for (int category = 0; category < numCategories; category++) {
            cumulativeProbability = cumulativeProbability.plus(p.slice(0, category));
            cumulativeProbabilities.add(cumulativeProbability);
        }
        return cumulativeProbabilities;
    }

    @Override
    public DoubleTensor logProb(IntegerTensor k) {
        int[] expectedShape = p.getShape();
        TensorShapeValidation.checkAllShapesMatch(
            String.format("Shape mismatch. k: %s, p: %s",
                Arrays.toString(k.getShape()),
                Arrays.toString(expectedShape)),
            k.getShape(), expectedShape
        );
        Preconditions.checkArgument(
            k.sum(0).elementwiseEquals(this.n).allTrue(),
            String.format("Inputs %s must sum to n = %s", k, this.n)
        );
        Preconditions.checkArgument(
            k.greaterThanOrEqual(0).allTrue(),
            String.format("Inputs %s cannot be negative", k)
        );

        DoubleTensor gammaN = n.plus(1).toDouble().logGammaInPlace();
        DoubleTensor gammaKs = k.plus(1).toDouble().logGammaInPlace().sum(0);
        DoubleTensor kLogP = p.log().timesInPlace(k.toDouble()).sum(0);
        return kLogP.plusInPlace(gammaN).minusInPlace(gammaKs);
    }
}
