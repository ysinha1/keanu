package io.improbable.keanu.vertices.generic.nonprobabilistic;

import static org.hamcrest.MatcherAssert.assertThat;

import static io.improbable.keanu.tensor.TensorMatchers.isScalarWithValue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

public class ConstantTimesLoopVertexTest {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private LoopVertex<Double> loop;
    private ConstantIntegerVertex numTimes;

    @Before
    public void setSeed() {
        KeanuRandom.setDefaultRandomSeed(0);
    }

    @Before
    public void setupLoop() {
        numTimes = ConstantVertex.of(4);
        loop = Loop
            .startingFrom(ConstantVertex.of(0.))
            .apply(buildPlusPlusIncrement())
            .times(numTimes);
    }

    @Test
    public void youCanLoopNTimes() {
        log.info("***** getValue");
        assertThat(loop.getValue(), isScalarWithValue(4.));
        log.info("***** Sample");
        assertThat(loop.getValue(), isScalarWithValue(4.));
    }

    @Test
    public void youCanChangeTheNumberOfTimesToLoop() {
        log.info("***** setAndCascade");
        numTimes.setAndCascade(6);
        log.info("***** getValue");
        assertThat(loop.getValue(), isScalarWithValue(6.));
        log.info("***** Sample");
        assertThat(loop.getValue(), isScalarWithValue(6.));
    }
    @Test
    public void youCanObserveTheNumberOfTimesToLoop() {
        log.info("***** setAndCascade");
        numTimes.observe(6);
        log.info("***** getValue");
        assertThat(loop.getValue(), isScalarWithValue(6.));
        log.info("***** Sample");
        assertThat(loop.getValue(), isScalarWithValue(6.));
    }

    @Test
    public void theLambdaCanBeDependentOnTheLoopsParents() {
        log.info("Setting up");
        DoubleVertex start = new GaussianVertex(0, 1);
        LoopVertex<Double> loop = Loop
            .startingFrom(start)
            .apply(buildPlusPlusIncrement())
            .times(ConstantVertex.of(4));
        loop.addParent(start);
        log.info("getValue");
        assertThat(loop.getValue(), isScalarWithValue(5.055319473180422));
        log.info("Sample");
        assertThat(loop.getValue(), isScalarWithValue(5.055319473180422));
        log.info("getValue");
        assertThat(loop.getValue(), isScalarWithValue(5.055319473180422));
        log.info("Sample");
        assertThat(loop.getValue(), isScalarWithValue(5.055319473180422));
    }


    @Test
    public void theLambdaCanBeProbabilistic() {
        log.info("Setting up");
        LoopVertex<Double> loop = Loop
            .startingFrom(ConstantVertex.of(0.))
            .apply(buildPlusVariableIncrement())
            .times(ConstantVertex.of(4));
        log.info("getValue");
        assertThat(loop.getValue(), isScalarWithValue(4.221277892721689));
        log.info("Sample");
        assertThat(loop.getValue(), isScalarWithValue(4.221277892721689));
        log.info("getValue");
        assertThat(loop.getValue(), isScalarWithValue(4.221277892721689));
        log.info("Sample");
        assertThat(loop.getValue(), isScalarWithValue(4.221277892721689));
    }

    @Test
    public void youCanCalculateTheDual() {
        log.info("***** Setting up");
        UniformVertex x = new UniformVertex(2., 2.);
        LoopVertex<Double> loop = Loop
            .startingFrom(x)
            .apply(buildPolynomial())
            .times(ConstantVertex.of(2));
        log.info("***** getValue");
        assertThat(loop.getValue(), isScalarWithValue(26.)); // x -> (x^2 + 1)^2 + 1 = x^4 + 2x^2 + 2
        DualNumber dualNumber = loop.getDualNumber();
        assertThat(loop.getValue(), isScalarWithValue(26.));
        Vertex input = Iterables.getOnlyElement(buildPolynomial().getInputVertices());
        assertThat(dualNumber.getPartialDerivatives().withRespectTo(x), isScalarWithValue(40.)); // x -> 4x^3 + 4x
    }

    private BayesianNetwork buildPlusPlusIncrement() {
        PlaceholderVertex input = new PlaceholderVertex(1, 1);
        Vertex output = ConstantVertex.of(1.).plus(input);
        return new BayesianNetwork(output.getConnectedGraph());
    }

    private BayesianNetwork buildPolynomial() {
        PlaceholderVertex input = new PlaceholderVertex(1, 1);
        Vertex output = new CastDoubleVertex(input).pow(2.).plus(1.);
        return new BayesianNetwork(output.getConnectedGraph());
    }

    private BayesianNetwork buildPlusVariableIncrement() {
        PlaceholderVertex input = new PlaceholderVertex(1, 1);
        Vertex output = new GaussianVertex(0., 1.).plus(input);
        return new BayesianNetwork(output.getConnectedGraph());
    }
}
