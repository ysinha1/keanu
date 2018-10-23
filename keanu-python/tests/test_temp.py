import pytest
import keanu as kn
import numpy as np


@pytest.mark.parametrize("foo", [1])
def test_const_takes_ndarray(foo):
    ndarray = np.array([[1, 2], [3, 4]])
    v = kn.Const(ndarray)

