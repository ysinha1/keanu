from keanu.base import JavaObjectWrapper
from py4j.java_gateway import java_import
from keanu.context import KeanuContext

# Minimum repro case for the issue.
# For reproducing the Windows gradle build issue.
k = KeanuContext().jvm_view()

java_import(k, "io.improbable.keanu.algorithms.variational.optimizer.gradient.TestOptimiser")

class Thing(JavaObjectWrapper):
    def __init__(self, x):
        super(Thing, self).__init__(k.TestOptimiser, x)

def test_optimiser():
    optimiser = Thing(1)
    # assert False

