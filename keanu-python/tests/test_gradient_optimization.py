from keanu.base import JavaObjectWrapper
from keanu.context import KeanuContext

# Minimum repro case for the issue.
# For reproducing the Windows gradle build issue.
class Thing(JavaObjectWrapper):
    def __init__(self, x):
        super(Thing, self).__init__(KeanuContext().jvm_view().TestOptimiser, x)

def test_optimiser():
    optimiser = Thing(1)
    # assert False

