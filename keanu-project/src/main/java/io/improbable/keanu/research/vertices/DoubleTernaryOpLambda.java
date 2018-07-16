package io.improbable.keanu.research.vertices;

import io.improbable.keanu.research.TriFunction;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.NonProbabilisticDouble;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

import java.util.Map;
import java.util.function.Function;

public class DoubleTernaryOpLambda<A, B, C> extends NonProbabilisticDouble {

    protected final Vertex<A> a;
    protected final Vertex<B> b;
    protected final Vertex<C> c;
    protected final TriFunction<A, B, C, DoubleTensor> op;
    protected final Function<Map<Vertex, DualNumber>, DualNumber> dualNumberSupplier;


    public DoubleTernaryOpLambda(int[] shape, Vertex<A> a, Vertex<B> b, Vertex<C> c,
                                 TriFunction<A, B, C, DoubleTensor> op,
                                 Function<Map<Vertex, DualNumber>, DualNumber> dualNumberCalculation) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.op = op;
        this.dualNumberSupplier = dualNumberCalculation;
        setParents(a, b, c);
        setValue(DoubleTensor.placeHolder(shape));
    }

    @Override
    public DoubleTensor sample(KeanuRandom random) {
        return op.apply(a.sample(random), b.sample(random), c.sample(random));
    }

    @Override
    public DoubleTensor getDerivedValue() {
        return op.apply(a.getValue(), b.getValue(), c.getValue());
    }

    @Override
    protected DualNumber calculateDualNumber(Map<Vertex, DualNumber> dualNumbers) {
        if (dualNumberSupplier != null) {
            return dualNumberSupplier.apply(dualNumbers);
        }

        throw new UnsupportedOperationException();
    }
}
