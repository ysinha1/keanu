package io.improbable.keanu.algorithms.mcmc.adaptive;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.VertexId;

public interface GaussianAdaptiveMcMcStrategy extends AdaptiveMcMcStrategy  {
    public DoubleTensor getSigmaValue(VertexId id);
}
