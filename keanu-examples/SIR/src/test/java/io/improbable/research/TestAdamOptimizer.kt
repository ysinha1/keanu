package io.improbable.research

import org.apache.commons.math3.optim.InitialGuess
import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.SimpleValueChecker
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer
import org.junit.Test
import java.util.*
import kotlin.math.abs

class TestAdamOptimizer {
    @Test
    fun testOptimizer() {
        var rand = Random()
        var myOpt = AdamOptimizer(SimpleValueChecker(1e-15, 1e-15))
//        var myOpt = NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE, SimpleValueChecker(1e-8, 1e-8))

        val fitness = ObjectiveFunction {point ->
            point[0]*point[0] + point[1]*point[1]
        }
        val gradient = ObjectiveFunctionGradient { point ->
            doubleArrayOf(2.0*point[0], 2.0*point[1])
//            doubleArrayOf(2.0*point[0] + 0.01*rand.nextGaussian(), 2.0*point[1] + 0.01*rand.nextGaussian())
        }

        val startingPoint = doubleArrayOf(1.23, 2.34)

        val pointValuePair = myOpt.optimize(
            MaxEval(2000),
            fitness,
            gradient,
            GoalType.MINIMIZE,
            InitialGuess(startingPoint)
        )

        println("${pointValuePair.point[0]} ${pointValuePair.point[1]}")
        assert(abs(pointValuePair.point[0]) < 1e-6)
        assert(abs(pointValuePair.point[1]) < 1e-6)

    }
}