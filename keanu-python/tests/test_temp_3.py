from keanu import Vertex, KeanuContext
import numpy as np
import pytest


@pytest.fixture
def jvm_view():
    jvm_view = KeanuContext().jvm_view()
    return jvm_view



def test_can_pass_ndarray_to_vertex(jvm_view):
    gaussian = Vertex(jvm_view.GaussianVertex, (np.array([[0.1, 0.4]]), np.array([[0.4, 0.5]])))
