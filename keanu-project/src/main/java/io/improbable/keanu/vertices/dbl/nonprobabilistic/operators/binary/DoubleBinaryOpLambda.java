package io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.NonProbabilisticDouble;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

import java.util.Map;
import io.improbable.keanu.vertices.dbltensor.KeanuRandom;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DoubleBinaryOpLambda<A, B> extends NonProbabilisticDouble {

    protected final Vertex<A> a;
    protected final Vertex<B> b;
    protected final BiFunction<A, B, Double> op;
    protected final Function<Map<Vertex, DualNumber>, DualNumber> dualNumberCalculation;

    public DoubleBinaryOpLambda(Vertex<A> a, Vertex<B> b, BiFunction<A, B, Double> op, Function<Map<Vertex, DualNumber>, DualNumber> dualNumberCalculation) {
        this.a = a;
        this.b = b;
        this.op = op;
        this.dualNumberCalculation = dualNumberCalculation;
        setParents(a, b);
    }

    public DoubleBinaryOpLambda(Vertex<A> a, Vertex<B> b, BiFunction<A, B, Double> op) {
        this(a, b, op, null);
    }

    @Override
    public Double sample(KeanuRandom random) {
        return op.apply(a.sample(random), b.sample(random));
    }

    @Override
    public Double getDerivedValue() {
        return op.apply(a.getValue(), b.getValue());
    }

    @Override
    public DualNumber calculateDualNumber(Map<Vertex, DualNumber> dualNumbers) {
        if (dualNumberCalculation != null) {
            return dualNumberCalculation.apply(dualNumbers);
        }

        throw new UnsupportedOperationException();
    }
}
