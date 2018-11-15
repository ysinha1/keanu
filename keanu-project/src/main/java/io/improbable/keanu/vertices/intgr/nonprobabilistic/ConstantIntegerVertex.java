package io.improbable.keanu.vertices.intgr.nonprobabilistic;


import io.improbable.keanu.annotation.ExportVertexToPythonBindings;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.NonProbabilistic;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public class ConstantIntegerVertex extends IntegerVertex implements NonProbabilistic<IntegerTensor>, ConstantVertex {

    @ExportVertexToPythonBindings
    public ConstantIntegerVertex(IntegerTensor constant) {
        super(constant.getShape());
        setValue(constant);
    }

    public ConstantIntegerVertex(int[] vector) {
        this(IntegerTensor.create(vector));
    }

    public ConstantIntegerVertex(int constant) {
        this(IntegerTensor.scalar(constant));
    }

    @Override
    public IntegerTensor sample(KeanuRandom random) {
        return getValue();
    }

    @Override
    public IntegerTensor calculate() {
        return getValue();
    }
}
