package io.improbable.keanu.research.vertices;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.DoubleUnaryOpLambda;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

import java.util.HashMap;
import java.util.Map;

public class DoubleTensorArrayIndexingVertex extends DoubleUnaryOpLambda<DoubleTensor> {

    public DoubleTensorArrayIndexingVertex(Vertex<DoubleTensor> input, int index) {
        super(Tensor.SCALAR_SHAPE, input,
            (DoubleTensor in) -> DoubleTensor.scalar(in.getValue(index)),
            (Map<Vertex, DualNumber> duals) -> {
                DualNumber inDual = duals.get(input);
                return extractDualNumberByIndex(inDual, index);
            });
    }

    private static DualNumber extractDualNumberByIndex(DualNumber dual, int index) {
        double extractedValue = dual.getValue().getValue(index);
        DoubleTensor extractedValueTensor = DoubleTensor.create(extractedValue, Tensor.SCALAR_SHAPE);
        Map<Long, DoubleTensor> extractedPartialDerivatives = new HashMap<>();
        for (Map.Entry<Long, DoubleTensor> entry : dual.getPartialDerivatives().asMap().entrySet()) {
            double pd = entry.getValue().getValue(index);
            DoubleTensor pdTensor = DoubleTensor.create(pd, Tensor.SCALAR_SHAPE);
            extractedPartialDerivatives.put(entry.getKey(), pdTensor);
        }

        return new DualNumber(extractedValueTensor, extractedPartialDerivatives);
    }
}
