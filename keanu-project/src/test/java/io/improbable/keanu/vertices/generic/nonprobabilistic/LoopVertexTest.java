package io.improbable.keanu.vertices.generic.nonprobabilistic;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public class LoopVertexTest {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Before
    public void setSeed() {
        KeanuRandom.setDefaultRandomSeed(0);
    }

    @Test
    public void youCanLoopNTimes() {
        log.info("***** Setting up");
        DoubleVertex start = ConstantVertex.of(0.);
        BayesianNetwork lambda = buildPlusPlusIncrement();
        IntegerVertex times = ConstantVertex.of(4);
        NumberLoopVertex<Double> loop = Loop.startingFrom(start).apply(lambda).times(times);
        log.info("***** getValue");
        assertEquals(4., loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("***** Sample");
        assertEquals(4., loop.sample().scalar().doubleValue(), 1e-8);
        log.info("***** setAndCascade");
        times.setAndCascade(6);
        log.info("***** getValue");
        assertEquals(6., loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("***** Sample");
        assertEquals(6., loop.sample().scalar().doubleValue(), 1e-8);
    }

    @Test
    public void youCanChangeTheNumberOfTimesToLoop() {
        log.info("***** Setting up");
        DoubleVertex start = ConstantVertex.of(0.);
        BayesianNetwork lambda = buildPlusPlusIncrement();
        IntegerVertex times = ConstantVertex.of(4);
        NumberLoopVertex<Double> loop = Loop.startingFrom(start).apply(lambda).times(times);
        log.info("***** setAndCascade");
        times.setAndCascade(6);
        log.info("***** getValue");
        assertEquals(6., loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("***** Sample");
        assertEquals(6., loop.sample().scalar().doubleValue(), 1e-8);
    }

    @Test
    public void youCanLoopWhileAConditionIsMet() {
        log.info("***** Setting up ******");
        DoubleVertex start = ConstantVertex.of(0.);
        BayesianNetwork lambda = buildPlusPlusIncrement();
        BoolVertex condition = new Flip(0.5);

        LoopVertex<Double> loop = Loop.startingFrom(start).apply(lambda).whilst(condition);
        log.info("****** Sample");
        assertEquals(5., loop.sample().scalar().doubleValue(), 1e-8);
        log.info("****** getValue");
        assertEquals(5., loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("****** observe");
        condition.observe(false);
        log.info("****** Sample");
        assertEquals(0., loop.sample().scalar().doubleValue(), 1e-8);
        log.info("****** getValue");
        assertEquals(0., loop.getValue().scalar().doubleValue(), 1e-8);
    }

    @Test
    public void theConditionCanBeDependentOnTheLambda() {
        log.info("Setting up");
        DoubleVertex start = ConstantVertex.of(0.);
        BayesianNetwork lambda = buildPlusPlusIncrement();
        BayesianNetwork condition = buildLessThanConstantNetwork();

        LoopVertex<Double> loop = Loop.startingFrom(start).apply(lambda).whilst(condition);
        log.info("Sample");
        assertEquals(5, loop.sample().scalar().doubleValue(), 1e-8);
        log.info("getValue");
        assertEquals(5, loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("Sample");
        assertEquals(5, loop.sample().scalar().doubleValue(), 1e-8);
        log.info("getValue");
        assertEquals(5, loop.getValue().scalar().doubleValue(), 1e-8);
    }

    @Test
    public void theConditionCanBeProbabilistic() {
        log.info("Setting up");
        DoubleVertex start = ConstantVertex.of(0.);
        BayesianNetwork lambda = buildPlusPlusIncrement();
        BayesianNetwork condition = buildLessThanVariableNetwork();

        LoopVertex<Double> loop = Loop.startingFrom(start).apply(lambda).whilst(condition);
        log.info("Sample");
        assertEquals(11, loop.sample().scalar().doubleValue(), 1e-8);
        log.info("getValue");
        assertEquals(11, loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("Sample");
        assertEquals(9, loop.sample().scalar().doubleValue(), 1e-8);
        log.info("getValue");
        assertEquals(9, loop.getValue().scalar().doubleValue(), 1e-8);
    }


    @Test
    public void theLambdaCanBeDependentOnTheLoopsParents() {
        log.info("Setting up");
        DoubleVertex x = new GaussianVertex(0, 1);
        BayesianNetwork lambda = buildPlusPlusIncrement();
        BayesianNetwork condition = buildLessThanConstantNetwork();

        LoopVertex<Double> loop = Loop.startingFrom(x).apply(lambda).whilst(condition);
        loop.addParent(x);
        log.info("Sample");
        assertEquals(5.05531947, loop.sample().scalar().doubleValue(), 1e-8);
        log.info("getValue");
        assertEquals(5.05531947, loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("Sample");
        assertEquals(5.05531947, loop.sample().scalar().doubleValue(), 1e-8);
        log.info("getValue");
        assertEquals(5.05531947, loop.getValue().scalar().doubleValue(), 1e-8);
    }


    @Test
    public void youCanCalculateTheDual() {
        log.info("***** Setting up");
        DoubleVertex x = new UniformVertex(2., 2.);
        BayesianNetwork lambda = buildPolynomial(); // x -> x^2 + 1
        IntegerVertex times = ConstantVertex.of(2);

        LoopVertex<Double> loop = Loop.startingFrom(x).apply(lambda).times(times);
        log.info("***** getValue");
        assertEquals(26., loop.getValue().scalar().doubleValue(), 1e-8); // x -> (x^2 + 1)^2 + 1 = x^4 + 2x^2 + 2
        DualNumber dualNumber = loop.getDualNumber();
        assertEquals(26., dualNumber.getValue().scalar(), 1e-8);
        Vertex input = Iterables.getOnlyElement(lambda.getInputVertices());
        assertEquals(40., dualNumber.getPartialDerivatives().withRespectTo(x).scalar().doubleValue(), 1e-8); // x -> 4x^3 + 4x

    }

    @Test
    public void theLambdaCanBeProbabilistic() {
        log.info("Setting up");
        DoubleVertex start = ConstantVertex.of(0.);
        BayesianNetwork lambda = buildPlusVariableIncrement();
        BayesianNetwork condition = buildLessThanConstantNetwork();

        LoopVertex<Double> loop = Loop.startingFrom(start).apply(lambda).whilst(condition);
        log.info("Sample");
        assertEquals(5.14940222, loop.sample().scalar().doubleValue(), 1e-8);
        log.info("getValue");
        assertEquals(5.42348173, loop.getValue().scalar().doubleValue(), 1e-8);
        log.info("Sample");
        assertEquals(5.06548962, loop.sample().scalar().doubleValue(), 1e-8);
        log.info("getValue");
        assertEquals(5.42348173, loop.getValue().scalar().doubleValue(), 1e-8);
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

    private BayesianNetwork buildLessThanConstantNetwork() {
        PlaceholderVertex input = new PlaceholderVertex(1, 1);
        BoolVertex output = ConstantVertex.of(5).greaterThan(input);
        return new BayesianNetwork(output.getConnectedGraph());
    }

    private BayesianNetwork buildLessThanVariableNetwork() {
        PlaceholderVertex input = new PlaceholderVertex(1, 1);
        BoolVertex output = new GaussianVertex( 10., 2.).greaterThan(input);
        return new BayesianNetwork(output.getConnectedGraph());

    }
}
