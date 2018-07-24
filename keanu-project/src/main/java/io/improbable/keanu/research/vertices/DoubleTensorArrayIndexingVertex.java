package io.improbable.keanu.research.vertices;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.DoubleUnaryOpLambda;

import java.util.*;
import java.util.stream.Collectors;

public class DoubleTensorArrayIndexingVertex extends DoubleUnaryOpLambda<DoubleTensor> {

    public DoubleTensorArrayIndexingVertex(Vertex<DoubleTensor> input, int[] valueIndex, int[] dualIndex) {
        super(Tensor.SCALAR_SHAPE, input,
            (DoubleTensor in) -> extractFromTensor(in, valueIndex),
            (Map<Vertex, DualNumber> duals) -> {
                DualNumber inDual = duals.get(input);
                return extractDualNumberByIndex(inDual, valueIndex, dualIndex);
            });
    }

    private static DualNumber extractDualNumberByIndex(DualNumber dual, int[] valueIndex, int[] dualIndex) {
        System.out.println("Extracting dual number");
        DoubleTensor extractedValueTensor = extractFromTensor(dual.getValue(), valueIndex);
        Map<Long, DoubleTensor> extractedPartialDerivatives = new HashMap<>();
        for (Map.Entry<Long, DoubleTensor> entry : dual.getPartialDerivatives().asMap().entrySet()) {
            DoubleTensor extractedPartialDerivative = extractFromTensor(entry.getValue(), dualIndex);
            extractedPartialDerivatives.put(entry.getKey(), extractedPartialDerivative);
        }

        return new DualNumber(extractedValueTensor, extractedPartialDerivatives);
    }

    private static DoubleTensor extractFromTensor(DoubleTensor doubleTensor, int[] index) {
        int[] shape = doubleTensor.getShape();
        int extractedShapeLength = shape.length - index.length;
//        System.out.println("Extracting shape from " + extractedShapeLength + " to " + shape.length);
        int[] extractedShape = Arrays.copyOfRange(shape, extractedShapeLength, shape.length);

        if (extractedShapeLength == 0) {
            return DoubleTensor.create(doubleTensor.getValue(index), Tensor.SCALAR_SHAPE);
        }


        System.out.println("Extracted shape = " + Arrays.toString(extractedShape));
        List<int[]> indexes = new ArrayList<>();

        discoverIndexes(indexes, shape, new int[shape.length], 0);
        List<int[]> matchingIndexes = indexes.stream().filter(i -> startMatches(index, i)).collect(Collectors.toList());

        System.out.println("Indexes:");
        for (int[] idx : indexes) {
            System.out.println(Arrays.toString(idx));
        }

        System.out.println("Matching indexes:");
        for (int[] idx : matchingIndexes) {
            System.out.println(Arrays.toString(idx));
        }

        DoubleTensor extractedTensor = DoubleTensor.zeros(extractedShape);
        System.out.println("Extracted tensor has shape " + Arrays.toString(extractedTensor.getShape()));

        for (int[] matchingIndex : matchingIndexes) {
            int[] extractedIndex = Arrays.copyOfRange(matchingIndex, extractedShapeLength, matchingIndex.length);
            double value = doubleTensor.getValue(matchingIndex);
            System.out.println("Setting index " + Arrays.toString(extractedIndex) + " to " + value + " from index " + Arrays.toString(matchingIndex));
            extractedTensor.setValue(value, extractedIndex);
        }

        System.out.println();
        System.out.println();
        return extractedTensor;
    }

    private static void discoverIndexes(List<int[]> indexes, int[] remainingShape, int[] index, int indexIdx, int... startOfIndex) {
        if (remainingShape.length == 0) {
            indexes.add(index);
        } else {
            int[] subShape = Arrays.copyOfRange(remainingShape, 1, remainingShape.length);
            for (int i = 0; i < remainingShape[0]; i++) {
                int[] discoveredIndex = index.clone();
                discoveredIndex[indexIdx] = i;
                discoverIndexes(indexes, subShape, discoveredIndex, indexIdx + 1, startOfIndex);
            }
        }
    }

    private static boolean startMatches(int[] toMatch, int[] index) {
        for (int i = 0; i < toMatch.length; i++) {
            if (toMatch[i] != index[i]) {
                return false;
            }
        }

        return true;
    }

    private int[] copyListToArray(List<Integer> index) {
        int[] array = new int[index.size()];
        for (int i = 0; i < index.size(); i++) {
            array[i] = index.get(i);
        }

        return array;
    }

    public static void main(String[] args) {
        int[] shape = {1, 3, 1, 3};
        int[] index = {0, 0, 0, 0};
        List<int[]> indexes = new ArrayList<>();
        discoverIndexes(indexes, shape, index, 0, 0, 1);
        int[] target = {0, 2};
        List<int[]> matchingIndexes = indexes.stream().filter(i -> startMatches(target, i)).collect(Collectors.toList());

        for (int[] idx : matchingIndexes) {
            System.out.println(Arrays.toString(idx));
        }
    }
}
