## This is a generated file. DO NOT EDIT.

from py4j.java_gateway import java_import
from keanu.context import KeanuContext
from keanu.vertex import Vertex

k = KeanuContext()


java_import(k.jvm_view(), "io.improbable.keanu.vertices.bool.nonprobabilistic.ConstantBoolVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.bool.nonprobabilistic.operators.binary.compare.EqualsVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.bool.nonprobabilistic.operators.binary.compare.GreaterThanOrEqualVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.bool.nonprobabilistic.operators.binary.compare.GreaterThanVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.bool.nonprobabilistic.operators.binary.compare.LessThanOrEqualVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.bool.nonprobabilistic.operators.binary.compare.LessThanVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.bool.nonprobabilistic.operators.binary.compare.NotEqualsVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.DoubleIfVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.AdditionVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.DifferenceVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.DivisionVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.MultiplicationVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.PowerVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.AbsVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.CeilVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.FloorVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.RoundVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.probabilistic.CauchyVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.probabilistic.ExponentialVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.probabilistic.GammaVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.intgr.nonprobabilistic.operators.binary.IntegerDivisionVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.intgr.probabilistic.PoissonVertex")
java_import(k.jvm_view(), "io.improbable.keanu.vertices.intgr.probabilistic.UniformIntVertex")


def ConstantBool(constant) -> k.jvm_view().ConstantBoolVertex:
    return Vertex(k.jvm_view().ConstantBoolVertex, constant)


def Equals(a, b) -> k.jvm_view().EqualsVertex:
    return Vertex(k.jvm_view().EqualsVertex, a, b)


def GreaterThanOrEqual(a, b) -> k.jvm_view().GreaterThanOrEqualVertex:
    return Vertex(k.jvm_view().GreaterThanOrEqualVertex, a, b)


def GreaterThan(a, b) -> k.jvm_view().GreaterThanVertex:
    return Vertex(k.jvm_view().GreaterThanVertex, a, b)


def LessThanOrEqual(a, b) -> k.jvm_view().LessThanOrEqualVertex:
    return Vertex(k.jvm_view().LessThanOrEqualVertex, a, b)


def LessThan(a, b) -> k.jvm_view().LessThanVertex:
    return Vertex(k.jvm_view().LessThanVertex, a, b)


def NotEquals(a, b) -> k.jvm_view().NotEqualsVertex:
    return Vertex(k.jvm_view().NotEqualsVertex, a, b)


def CastDouble(inputVertex) -> k.jvm_view().CastDoubleVertex:
    return Vertex(k.jvm_view().CastDoubleVertex, inputVertex)


def ConstantDouble(constant) -> k.jvm_view().ConstantDoubleVertex:
    return Vertex(k.jvm_view().ConstantDoubleVertex, constant)


def DoubleIf(shape, predicate, thn, els) -> k.jvm_view().DoubleIfVertex:
    return Vertex(k.jvm_view().DoubleIfVertex, shape, predicate, thn, els)


def Addition(left, right) -> k.jvm_view().AdditionVertex:
    return Vertex(k.jvm_view().AdditionVertex, left, right)


def Difference(left, right) -> k.jvm_view().DifferenceVertex:
    return Vertex(k.jvm_view().DifferenceVertex, left, right)


def Division(left, right) -> k.jvm_view().DivisionVertex:
    return Vertex(k.jvm_view().DivisionVertex, left, right)


def Multiplication(left, right) -> k.jvm_view().MultiplicationVertex:
    return Vertex(k.jvm_view().MultiplicationVertex, left, right)


def Power(base, exponent) -> k.jvm_view().PowerVertex:
    return Vertex(k.jvm_view().PowerVertex, base, exponent)


def Abs(inputVertex) -> k.jvm_view().AbsVertex:
    return Vertex(k.jvm_view().AbsVertex, inputVertex)


def Ceil(inputVertex) -> k.jvm_view().CeilVertex:
    return Vertex(k.jvm_view().CeilVertex, inputVertex)


def Floor(inputVertex) -> k.jvm_view().FloorVertex:
    return Vertex(k.jvm_view().FloorVertex, inputVertex)


def Round(inputVertex) -> k.jvm_view().RoundVertex:
    return Vertex(k.jvm_view().RoundVertex, inputVertex)


def Cauchy(location, scale) -> k.jvm_view().CauchyVertex:
    return Vertex(k.jvm_view().CauchyVertex, location, scale)


def Exponential(lambdaVertex) -> k.jvm_view().ExponentialVertex:
    return Vertex(k.jvm_view().ExponentialVertex, lambdaVertex)


def Gamma(theta, k) -> k.jvm_view().GammaVertex:
    return Vertex(k.jvm_view().GammaVertex, theta, k)


def Gaussian(mu, sigma) -> k.jvm_view().GaussianVertex:
    return Vertex(k.jvm_view().GaussianVertex, mu, sigma)


def Uniform(xMin, xMax) -> k.jvm_view().UniformVertex:
    return Vertex(k.jvm_view().UniformVertex, xMin, xMax)


def ConstantInteger(constant) -> k.jvm_view().ConstantIntegerVertex:
    return Vertex(k.jvm_view().ConstantIntegerVertex, constant)


def IntegerDivision(a, b) -> k.jvm_view().IntegerDivisionVertex:
    return Vertex(k.jvm_view().IntegerDivisionVertex, a, b)


def Poisson(mu) -> k.jvm_view().PoissonVertex:
    return Vertex(k.jvm_view().PoissonVertex, mu)


def UniformInt(min, max) -> k.jvm_view().UniformIntVertex:
    return Vertex(k.jvm_view().UniformIntVertex, min, max)
