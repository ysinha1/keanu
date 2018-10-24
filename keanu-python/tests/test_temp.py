from keanu import Const
import numpy as np


def test_const_takes_ndarray(foo):
    ndarray = np.array([[1, 2], [3, 4]])
    v = Const(ndarray)

