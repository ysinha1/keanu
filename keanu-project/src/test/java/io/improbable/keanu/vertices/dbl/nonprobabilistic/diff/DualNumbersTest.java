package io.improbable.keanu.vertices.dbl.nonprobabilistic.diff;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;

import static io.improbable.keanu.tensor.TensorMatchers.isScalarWithValue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.LogVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;

public class DualNumbersTest {

    DoubleVertex vA;
    DoubleVertex vB;

    DoubleVertex variable;

    @Before
    public void setup() {
        vA = new GaussianVertex(1.0, 0.0);
        vB = new GaussianVertex(2.0, 0.0);
        variable = new UniformVertex(0., 1.);  // any probabilistic vertex would do

        DoubleTensor t = DoubleTensor.eye(5);
        Double scalar = t.scalar();
        assertEquals(scalar, 1., 1e-10);
    }

    @Test
    public void theDerivativeOf2xIs2() {
        ConstantDoubleVertex y = ConstantVertex.of(2.);
        DoubleVertex result = variable.times(y);
        Tensor<Double> actual = result.getDualNumber().getPartialDerivatives().withRespectTo(variable);
        assertThat(actual, isScalarWithValue(new Double(2.)));
    }

    @Test
    public void theDerivativeOfXSquaredIs2X() {
        DoubleVertex x = new UniformVertex(0., 1.);
        DoubleVertex result = x.pow(2);
        DualNumber dualNumber = result.getDualNumber();
        assertThat(dualNumber.getPartialDerivatives().withRespectTo(x), isScalarWithValue(2. * x.getValue().scalar()));
    }

    @Test
    public void theDerivativeOfLogXIs1OverX() {
        DoubleVertex result = variable.log();
        DualNumber dualNumber = result.getDualNumber();
        assertThat(dualNumber.getPartialDerivatives().withRespectTo(variable), isScalarWithValue(1./variable.getValue().scalar()));
    }

    @Test
    public void theDualOfASumIsItsValue() {
        DoubleVertex result = ConstantVertex.of(6.).plus(ConstantVertex.of(7.));
        assertThat(result.getDualNumber().getValue(), isScalarWithValue(13.));
    }

    @Test
    public void theDerivativeWithRespectToAConstantIsZero() {
        ConstantDoubleVertex x = ConstantVertex.of(2.);
        ConstantDoubleVertex y = ConstantVertex.of(3.);
        DoubleVertex result = x.times(y).log();
        assertThat(result.getDualNumber().getPartialDerivatives().withRespectTo(x), isScalarWithValue(0.));
    }

    @Test
    public void theDualOfAProbabilisticVertexIsItsSampledValue() {
        assertThat(
            new UniformVertex(0., 1.).getDualNumber().getValue(),
            isScalarWithValue(both(greaterThan(0.)).and(lessThan(1.)))
        );
    }


    @Test
    public void whenYouObserveAVertexYouFixItsValue() {
        UniformVertex uniformVertex = new UniformVertex(0., 1.);
        uniformVertex.observe(0.42);
        assertThat(
            uniformVertex.getDualNumber().getValue(),
            isScalarWithValue(equalTo(0.42))
        );
    }

    @Test
    public void diffOverMultiply() {
        assertDiffIsCorrect(vA, vB, vA.multiply(vB));
    }

    @Test
    public void diffOverAddition() {
        assertDiffIsCorrect(vA, vB, vA.plus(vB));
    }

    @Test
    public void diffOverSubtraction() {
        assertDiffIsCorrect(vA, vB, vA.minus(vB));
    }

    @Test
    public void diffOverExponent() {
        assertDiffIsCorrect(vA, vB, vA.multiply(vB).exp());
    }

    @Test
    public void diffOverPlusMinusMultiplyCombination() {
        DoubleVertex vC = vA.plus(vB);
        DoubleVertex vD = vA.minus(vB);
        DoubleVertex vE = vC.multiply(vD);
        assertDiffIsCorrect(vA, vB, vE);
    }

    @Test
    public void diffOverPlusDivideMultiplyLogCombination() {
        DoubleVertex vC = vA.plus(vB);
        DoubleVertex vD = vA.divideBy(vB);
        DoubleVertex vE = vC.multiply(vD);
        assertDiffIsCorrect(vA, vB, new LogVertex(vE));
    }

    private void assertDiffIsCorrect(DoubleVertex vA, DoubleVertex vB, DoubleVertex vC) {

        double A = 1.0;
        double B = 2.0;

        vA.setValue(A);
        vB.setValue(B);
        vC.eval();

        DualNumber cDual = vC.getDualNumber();

        DoubleTensor C = cDual.getValue();
        Map<Long, DoubleTensor> dc = cDual.getPartialDerivatives().asMap();

        double da = 0.00000001;

        vA.setValue(vA.getValue().plus(da));
        vB.setValue(B);
        vC.eval();

        DoubleTensor dcdaApprox = (vC.getValue().minus(C)).div(da);

        assertEquals(dcdaApprox.scalar(), dc.get(vA.getId()).scalar(), 0.00001);

        double db = da;

        vA.setValue(A);
        vB.setValue(B + db);
        vC.eval();

        DoubleTensor dcdbApprox = (vC.getValue().minus(C)).div(db);

        assertEquals(dcdbApprox.scalar(), dc.get(vB.getId()).scalar(), 0.00001);
    }
}
