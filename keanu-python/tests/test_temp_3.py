from keanu import Vertex, KeanuContext
import numpy as np
import pytest


@pytest.fixture
def jvm_view():
    from py4j.java_gateway import java_import
    jvm_view = KeanuContext().jvm_view()
    java_import(jvm_view, "io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex")
    return jvm_view



def test_can_pass_ndarray_to_vertex(jvm_view):
    gaussian = Vertex(jvm_view.GaussianVertex, (np.array([[0.1, 0.4]]), np.array([[0.4, 0.5]])))
