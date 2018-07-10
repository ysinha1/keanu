package io.improbable.keanu.vertices.intgr.probabilistic;

import io.improbable.keanu.distributions.discrete.Categorical;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CategoricalVertexTest {

    private KeanuRandom random;

    @Before
    public void setup() {
        random = new KeanuRandom(1);
    }

    @Test
    public void samplesUniformlyAcrossEquallyLikelyCategories() {
        int sampleCount = 40000;
        IntegerVertex categories = ConstantVertex.of(new int[]{1, 2, 3, 4});
        DoubleVertex likelihoods = ConstantVertex.of(new double[]{0.25, 0.25, 0.25, 0.25});
        Map<IntegerTensor, Integer> categoryCount = new HashMap<>();

        for (int i = 0; i < sampleCount; i++) {
            IntegerTensor category = Categorical.sample(categories.getValue(), likelihoods.getValue(), random);
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }

        int quarterOfSampleCount = sampleCount / 4;
        for (Integer value : categoryCount.values()) {
            Assert.assertTrue(value > quarterOfSampleCount - 250 && value < quarterOfSampleCount + 250);
        }
    }

    @Test
    public void samplesFromOnlyLikelyCategory() {
        int sampleCount = 40000;
        IntegerVertex categories = ConstantVertex.of(new int[]{1, 10, 5, 50});
        DoubleVertex likelihoods = ConstantVertex.of(new double[]{0.0, 0.0, 0.0, 1.0});
        Map<IntegerTensor, Integer> categoryCount = new HashMap<>();

        for (int i = 0; i < sampleCount; i++) {
            IntegerTensor category = Categorical.sample(categories.getValue(), likelihoods.getValue(), random);
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }

        Assert.assertEquals((int) categoryCount.get(IntegerTensor.scalar(50)), sampleCount);
    }

    @Test
    public void samplesAcrossDifferentLikelihoods() {
        int sampleCount = 50000;
        IntegerVertex categories = ConstantVertex.of(new int[]{1, 2, 3, 4});
        DoubleVertex likelihoods = ConstantVertex.of(new double[]{0.1, 0.2, 0.5, 0.2});
        Map<Integer, Integer> categoryCount = new HashMap<>();

        for (int i = 0; i < sampleCount; i++) {
            IntegerTensor category = Categorical.sample(categories.getValue(), likelihoods.getValue(), random);
            categoryCount.put(category.scalar(), categoryCount.getOrDefault(category.scalar(), 0) + 1);
        }

        for (int i = 0; i < categories.getValue().getFlattenedView().size(); i++) {
            int category = categories.getValue().getFlattenedView().get(i);
            double likelihood = likelihoods.getValue().getFlattenedView().get(i);
            double weightedSampleCount = sampleCount * likelihood;

            Assert.assertTrue(categoryCount.get(category) > (weightedSampleCount - 250) &&
                categoryCount.get(category) < weightedSampleCount + 250
            );

        }
    }

    @Test
    public void samplesAcrossDifferentUnnormalisedLikelihoods() {
        int sampleCount = 50000;
        int multiplicationFactor = 10;
        IntegerVertex categories = ConstantVertex.of(new int[]{1, 2, 3, 4});
        DoubleVertex likelihoods = ConstantVertex.of(new double[]{0.1, 0.2, 0.5, 0.2}).times(multiplicationFactor);
        Map<Integer, Integer> categoryCount = new HashMap<>();

        for (int i = 0; i < sampleCount; i++) {
            IntegerTensor category = Categorical.sample(categories.getValue(), likelihoods.getValue(), random);
            categoryCount.put(category.scalar(), categoryCount.getOrDefault(category.scalar(), 0) + 1);
        }

        for (int i = 0; i < categories.getValue().getFlattenedView().size(); i++) {
            int category = categories.getValue().getFlattenedView().get(i);
            double likelihood = likelihoods.getValue().getFlattenedView().get(i);
            double weightedSampleCount = (sampleCount * likelihood) / multiplicationFactor;

            Assert.assertTrue(categoryCount.get(category) > (weightedSampleCount - 250) &&
                categoryCount.get(category) < weightedSampleCount + 250
            );

        }
    }


}
