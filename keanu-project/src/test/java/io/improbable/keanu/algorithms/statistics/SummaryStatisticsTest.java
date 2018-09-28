package io.improbable.keanu.algorithms.statistics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.util.ArrayUtil;

import com.google.common.collect.Lists;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.KeanuRandom;

public class SummaryStatisticsTest {

    SummaryStatistics stats = new SummaryStatistics();
    private int numDataPoints = 2;
    final int[] tensorShape = new int[] {2, 3};
    final int tensorLength = ArrayUtil.prod(tensorShape);;
    org.apache.commons.math3.stat.descriptive.SummaryStatistics[] apacheStats2 = new org.apache.commons.math3.stat.descriptive.SummaryStatistics[tensorLength];
    List<org.apache.commons.math3.stat.descriptive.SummaryStatistics> apacheStats = Lists.newArrayListWithCapacity(tensorLength);


    @Before
    public void setUp() throws Exception {
        KeanuRandom.setDefaultRandomSeed(0);

        for (int channel = 0; channel < tensorLength; channel++) {
            apacheStats.add(new org.apache.commons.math3.stat.descriptive.SummaryStatistics());
        }
        for (int iteration = 0; iteration < numDataPoints; iteration++) {
            double[] values = new double[tensorLength];
            for (int channel = 0; channel < tensorLength; channel++) {
                double value = KeanuRandom.getDefaultRandom().nextDouble();
                values[channel] = value;
                apacheStats.get(channel).addValue(value);
            }
            stats.addValue(DoubleTensor.create(values, tensorShape));
        }
    }

    @Test
    public void youCanGetTheCount() {
        double[] expectedValues = new double[tensorLength];
        for (int i = 0; i < tensorLength; i++) {
            expectedValues[i] = apacheStats.get(i).getN();
        }
        assertThat(stats.getCount().asFlatIntegerArray(), equalTo(DoubleTensor.create(expectedValues, tensorShape).asFlatIntegerArray()));
    }

    @Test
    public void youCanGetTheMean() {
        checkMethodsAgree(
            SummaryStatistics::getMean,
            org.apache.commons.math3.stat.descriptive.SummaryStatistics::getMean
        );    }

    @Test
    public void youCanGetTheVariance() {
        checkMethodsAgree(
            SummaryStatistics::getVariance,
            org.apache.commons.math3.stat.descriptive.SummaryStatistics::getVariance
        );    }

    @Test
    public void youCanGetTheStandardDeviation() {
        checkMethodsAgree(
            SummaryStatistics::getStandardDeviation,
            org.apache.commons.math3.stat.descriptive.SummaryStatistics::getStandardDeviation
            );
     }

    public void checkMethodsAgree(
        Function<SummaryStatistics, DoubleTensor> keanuMethod,
        Function<org.apache.commons.math3.stat.descriptive.SummaryStatistics, Double> apacheMethod) {
        double[] expectedValues = new double[tensorLength];
        for (int i = 0; i < tensorLength; i++) {
            expectedValues[i] = apacheMethod.apply(apacheStats.get(i));
        }
        assertThat(keanuMethod.apply(stats), equalTo(DoubleTensor.create(expectedValues, tensorShape)));
    }
}