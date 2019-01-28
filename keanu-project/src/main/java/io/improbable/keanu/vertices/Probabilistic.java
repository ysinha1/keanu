package io.improbable.keanu.vertices;

import io.improbable.keanu.algorithms.variational.optimizer.Variable;
import io.improbable.keanu.tensor.dbl.DoubleTensor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface Probabilistic<VALUE, VARIABLE> {

    /**
     * This is the natural log of the probability at the supplied value. In the
     * case of continuous vertices, this is actually the log of the density, which
     * will differ from the probability by a constant.
     *
     * @param value The supplied value.
     * @return The natural log of the probability function at the supplied value.
     * For continuous variables this is called the PDF (probability density function).
     * For discrete variables this is called the PMF (probability mass function).
     */
    double logProb(VALUE value);

    /**
     * The partial derivatives of the natural log prob.
     *
     * @param atValue       at a given value
     * @param withRespectTo list of parents to differentiate with respect to
     * @return the partial derivatives of the log of the probability function at the supplied value.
     * For continuous variables this is called the PDF (probability density function).
     * For discrete variables this is called the PMF (probability mass function).
     */
    Map<VARIABLE, DoubleTensor> dLogProb(VALUE atValue, Set<? extends VARIABLE> withRespectTo);

    default Map<VARIABLE, DoubleTensor> dLogProb(VALUE atValue, VARIABLE... withRespectTo) {
        return dLogProb(atValue, new HashSet<>(Arrays.asList(withRespectTo)));
    }

    VALUE getValue();

    void setValue(VALUE value);

    default double logProbAtValue() {
        return logProb(getValue());
    }

    default Map<VARIABLE, DoubleTensor> dLogProbAtValue(Set<? extends VARIABLE> withRespectTo) {
        return dLogProb(getValue(), withRespectTo);
    }

    default Map<VARIABLE, DoubleTensor> dLogProbAtValue(VARIABLE... withRespectTo) {
        return dLogProb(getValue(), withRespectTo);
    }
}
