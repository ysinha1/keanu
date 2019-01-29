package io.improbable.keanu.algorithms;

import io.improbable.keanu.tensor.dbl.DoubleTensor;

import java.util.Map;

public interface ProbabilisticModelWithGradient extends ProbabilisticModel {

    Map<? extends VariableReference, DoubleTensor> logProbGradients(Map<VariableReference, ?> inputs);

    Map<? extends VariableReference, DoubleTensor> logProbGradients();

    Map<? extends VariableReference, DoubleTensor> logLikelihoodGradients(Map<VariableReference, ?> inputs);

    Map<? extends VariableReference, DoubleTensor> logLikelihoodGradients();

}
