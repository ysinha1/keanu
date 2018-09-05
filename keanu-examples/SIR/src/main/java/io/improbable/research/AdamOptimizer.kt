package io.improbable.research

import org.apache.commons.math3.optim.ConvergenceChecker
import org.apache.commons.math3.optim.PointValuePair
import org.apache.commons.math3.optim.nonlinear.scalar.GradientMultivariateOptimizer
import java.lang.Math.pow
import kotlin.math.sqrt

class AdamOptimizer(checker: ConvergenceChecker<PointValuePair>?) : GradientMultivariateOptimizer(checker) {

    override fun doOptimize(): PointValuePair {
//        println("Starting doOptimize")
        val a = 0.01   // stepsize
        val b1 = 0.9    // first moment decay coefficient
        val b2 = 0.999  // second moment decay coefficient
        val e = 1e-8    // fudge factor

        val checker = this.convergenceChecker
        val n = startPoint.size
        var optimal = PointValuePair(startPoint, this.computeObjectiveValue(startPoint))
        var lastOptimal = PointValuePair(startPoint, optimal.value)
        var optPoint: DoubleArray
        val goal = this.goalType
        var firstMoment = DoubleArray(n, {0.0})
        var secondMoment = DoubleArray(n, {0.0})

        do {
            this.incrementIterationCount()
            optPoint = lastOptimal.pointRef
            lastOptimal = optimal
            val g = this.computeObjectiveGradient(optimal.pointRef)
//            println("gradient is ${g[0]} ${g[1]}")
            for(i in 0 until n) {
                firstMoment[i] = b1*firstMoment[i] + (1.0-b1)*g[i]
                secondMoment[i] = b2*secondMoment[i] + (1.0-b2)*g[i]*g[i]
                val mi = firstMoment[i]/(1-pow(b1,this.getIterations().toDouble()))
                val vi = secondMoment[i]/(1-pow(b2,this.getIterations().toDouble()))
                optPoint[i] = optimal.pointRef[i] - a*mi/(sqrt(vi) + e)
            }
            optimal = PointValuePair(optPoint, this.computeObjectiveValue(optPoint), false)
//            println("(${lastOptimal.point[0]}, ${lastOptimal.point[1]}):${lastOptimal.value} -> (${optimal.point[0]}, ${optimal.point[1]}):${optimal.value}")
        } while(!checker.converged(this.getIterations(), lastOptimal, optimal))
        return optimal
    }
}