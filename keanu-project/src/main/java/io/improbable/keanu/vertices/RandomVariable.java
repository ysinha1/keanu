package io.improbable.keanu.vertices;

import io.improbable.keanu.algorithms.Variable;
import io.improbable.keanu.network.VariableState;

public interface RandomVariable<Value, State extends VariableState> extends Probabilistic<Value>, Variable<Value, State> {

}