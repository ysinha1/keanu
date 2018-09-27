package io.improbable.keanu.algorithms.mcmc.adaptive;

import io.improbable.keanu.tensor.dbl.DoubleTensor;

public interface GaussianAdaptiveMcMcStrategy extends AdaptiveMcMcStrategy  {
    public DoubleTensor getSigmaValue();
}
