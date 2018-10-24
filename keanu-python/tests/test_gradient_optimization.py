import pytest
from examples import thermometers
from keanu.context import KeanuContext
from keanu import x_TestThing
from py4j.java_gateway import get_field
from py4j.protocol import Py4JJavaError

# Minimum repro case for the issue.
def test_optimiser():
    optimiser = x_TestThing(1)
    # assert False

