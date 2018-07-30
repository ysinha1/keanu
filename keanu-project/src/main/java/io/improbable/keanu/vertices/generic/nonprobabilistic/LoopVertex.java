package io.improbable.keanu.vertices.generic.nonprobabilistic;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.vertices.dbl.Differentiable;

public abstract class LoopVertex<T> extends NonProbabilistic<Tensor<T>> implements Differentiable {
}
