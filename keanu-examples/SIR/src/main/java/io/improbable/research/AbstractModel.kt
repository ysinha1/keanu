package io.improbable.research

import io.improbable.keanu.tensor.dbl.DoubleTensor
import io.improbable.keanu.vertices.Vertex
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.PartialDerivatives
import io.improbable.keanu.vertices.generic.nonprobabilistic.If
import org.apache.commons.math3.distribution.PoissonDistribution
import org.apache.commons.math3.random.MersenneTwister

class AbstractModel(var rhoS : Double, var rhoI : Double, var rhoR : Double) {
    val Nsamples = 10000 // number of samples of the concrete model
    val rand = MersenneTwister()
    var concreteStates = createConcreteSamples()

    constructor(T : DoubleTensor) :
        this(T.getValue(0), T.getValue(1), T.getValue(2))
    {}

    fun step() {
        concreteStates = createConcreteSamples()
        concreteStates.forEach { model -> model.step() }
        setStateFromConcreteSamples()
    }

    fun step(startState : DoubleTensor) : DoubleTensor {
        setStateFromTensor(startState)
        step()
        return DoubleTensor.create(doubleArrayOf(rhoS, rhoI, rhoR))
    }

    fun calculateDualNumber(inDual : DualNumber?) : DualNumber? {
        if(inDual == null) return null
        setStateFromTensor(inDual.value)
        concreteStates = createConcreteSamples()
        val inConcreteStates = asMatrix(concreteStates)  // 3xNsamples matrix
        concreteStates.forEach { it.step() }
        setStateFromConcreteSamples()
        val outConcreteStates = asMatrix(concreteStates)
        val jacobian =
            ((inConcreteStates * outConcreteStates).sum(1).matrixMultiply(inDual.value.reciprocal().transpose())  -
                outConcreteStates.sum(1).matrixMultiply(DoubleTensor.ones(1,3))
                )/
                Nsamples.toDouble()
        return DualNumber(
            DoubleTensor.create(doubleArrayOf(rhoS, rhoI, rhoR)),
            inDual.partialDerivatives.asMap().mapValues{jacobian.matrixMultiply(it.value)}
        )
    }

    fun setStateFromTensor(T : DoubleTensor) {
        rhoS = T.getValue(0)
        rhoI = T.getValue(1)
        rhoR = T.getValue(2)
    }

    fun getStateAsTensor() : DoubleTensor {
        return DoubleTensor.create(doubleArrayOf(rhoS, rhoI, rhoR))
    }


    fun setStateFromConcreteSamples() {
        rhoS = concreteStates.sumBy { model -> model.S }/Nsamples.toDouble()
        rhoI = concreteStates.sumBy { model -> model.I }/Nsamples.toDouble()
        rhoR = concreteStates.sumBy { model -> model.R }/Nsamples.toDouble()
    }


    fun asMatrix(samples : Array<SIRModel>) : DoubleTensor {
        val s = DoubleTensor.zeros(intArrayOf(3, samples.size))
        for(i in 0 until samples.size) {
            s.setValue(samples[i].S.toDouble(), 0, i)
            s.setValue(samples[i].I.toDouble(), 1, i)
            s.setValue(samples[i].R.toDouble(), 2, i)
        }
        return s
    }

    fun createConcreteSamples() : Array<SIRModel> {
        return Array<SIRModel>(Nsamples, {
            SIRModel(
                PoissonDistribution(rhoS).sample(),
                PoissonDistribution(rhoI).sample(),
                PoissonDistribution(rhoR).sample(),
                rand
            )
        })
    }
}