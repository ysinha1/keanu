import pytest
import numpy as np
import pandas as pd
from keanu.vertex import If, Bernoulli, Gaussian, Const, Double, Poisson, Integer, Boolean


@pytest.mark.parametrize("predicate", [
    True, np.array([True, False]), pd.Series([True, False]),
    Bernoulli(0.5), Const(np.array([True, False]))])
@pytest.mark.parametrize("data", [
    1., np.array([1., 2.]), pd.Series([1., 2.]),
    Const(1.), Const(np.array([1., 2.]))
])
def test_you_can_create_a_double_valued_if(data, predicate) -> None:
    thn = data
    els = data
    result = If(predicate, thn, els)
    assert type(result) == Double
    assert result.unwrap().getClass().getSimpleName() == "DoubleIfVertex"


@pytest.mark.parametrize("predicate", [
    True, np.array([True, False]), pd.Series([True, False]),
    Bernoulli(0.5), Const(np.array([True, False]))])
@pytest.mark.parametrize("data", [
    1, np.array([1, 2]), pd.Series([1, 2]),
    Const(1), Const(np.array([1, 2]))
])
def test_you_can_create_an_integer_valued_if(data, predicate) -> None:
    thn = data
    els = data
    result = If(predicate, thn, els)
    assert type(result) == Integer
    assert result.unwrap().getClass().getSimpleName() == "IntegerIfVertex"


@pytest.mark.parametrize("predicate", [
    True, np.array([True, False]), pd.Series([True, False]),
    Bernoulli(0.5), Const(np.array([True, False]))])
@pytest.mark.parametrize("data", [
    True, np.array([True, False]), pd.Series([True, False]),
    Const(True), Const(np.array([True, False]))
])
def test_you_can_create_a_boolean_valued_if(data, predicate) -> None:
    thn = data
    els = data
    result = If(predicate, thn, els)
    assert type(result) == Boolean
    assert result.unwrap().getClass().getSimpleName() == "BooleanIfVertex"
