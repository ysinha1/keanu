package io.improbable.research

import io.improbable.keanu.tensor.dbl.DoubleTensor
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber
import org.apache.commons.math3.distribution.PoissonDistribution
import org.apache.commons.math3.random.MersenneTwister
import java.util.*

class AbstractModel(var rhoS: Double, var rhoI: Double, var rhoR: Double) {
    val Nsamples = 1000 // number of samples of the concrete model
    val rand = MersenneTwister()
    var concreteStates = createConcreteSamples()

    var hasBeenCalled = 0

    constructor(T: DoubleTensor) :
        this(T.getValue(0), T.getValue(1), T.getValue(2)) {
    }

    constructor(s: DoubleTensor, i: DoubleTensor, r: DoubleTensor) :
        this(s.scalar(), i.scalar(), r.scalar()) {
    }

    fun step() {
        concreteStates = createConcreteSamples()
        concreteStates.forEach { model -> model.step() }
        setStateFromConcreteSamples()
    }

    fun step(startState: DoubleTensor): DoubleTensor {
        setStateFromTensor(startState)
        step()
        return DoubleTensor.create(doubleArrayOf(rhoS, rhoI, rhoR))
    }

    fun step(startS: Double, startI: Double, startR: Double): DoubleArray {
        setState(startS, startI, startR)
        step()
        return doubleArrayOf(rhoS, rhoI, rhoR)
    }

//    fun calculateDualNumber(sDual: DualNumber?, iDual: DualNumber?, rDual: DualNumber?,
//                            sId: Long, iId: Long, rId: Long): DualNumber? {
//        if (sDual == null || iDual == null || rDual == null) return null
//        setState(sDual.value.scalar(), iDual.value.scalar(), rDual.value.scalar())
//
//        concreteStates = createConcreteSamples()
//        println("Created ${concreteStates.size} concrete samples")
//        val inConcreteStates = asMatrix(concreteStates)  // 3xNsamples matrix
//        println("inConcreteStates has shape of ${Arrays.toString(inConcreteStates.shape)}")
//        concreteStates.forEach { it.step() }
//        setStateFromConcreteSamples()
//        val outConcreteStates = asMatrix(concreteStates)
//        println("outConcreteStates has shape of ${Arrays.toString(outConcreteStates.shape)}")
//
//        val inDualValue = DoubleTensor.create(doubleArrayOf(sDual.value.scalar(), iDual.value.scalar(), rDual.value.scalar()))
//        val jacobian = calculateJacobian(inConcreteStates, outConcreteStates, inDualValue)
//        println("Jacobian has shape ${Arrays.toString(jacobian.shape)}")
//
//        val values = DoubleTensor.create(doubleArrayOf(rhoS, rhoI, rhoR))
//
//        val sPD = if (sDual.partialDerivatives.asMap().isEmpty()) {
//            jacobian.matrixMultiply(DoubleTensor.ONE_SCALAR)
//        } else {
//            jacobian.matrixMultiply(sDual.partialDerivatives.asMap().values.first())
//        }
//
//        jacobian.matrixMultiply(iDual.partialDerivatives.asMap().values.first())
//        jacobian.matrixMultiply(rDual.partialDerivatives.asMap().values.first())
//
////        val partialDerivatives = inDual.partialDerivatives.asMap.mapValues {
////            jacobian.matrixMultiply(it.value)
////        }
//
//        val dual = DualNumber(values, partialDerivatives)
//
//        println("Dual has ${values.length} values and ${partialDerivatives.size} partial derivatives")
//
//        return dual
//    }
//
//    private fun dx0_dx1(inConcreteStates: DoubleTensor, outConcreteStates: DoubleTensor): Double {
//
//    }

    fun calculateDualNumber(inDual: DualNumber?, inId: Long): DualNumber? {
        hasBeenCalled++

        println("calculateDualNumber has been called $hasBeenCalled times")

        if (inDual == null) return null
        setStateFromTensor(inDual.value)

        concreteStates = createConcreteSamples()
//        println("Created ${concreteStates.size} concrete samples")
        val inConcreteStates = asMatrix(concreteStates)  // 3xNsamples matrix
//        println("inConcreteStates has shape of ${Arrays.toString(inConcreteStates.shape)}")
        concreteStates.forEach { it.step() }
        setStateFromConcreteSamples()
        val outConcreteStates = asMatrix(concreteStates)
//        println("outConcreteStates has shape of ${Arrays.toString(outConcreteStates.shape)}")

        println("inDual has ${inDual.value} values and ${inDual.partialDerivatives.asMap().size} partial derivatives")
        val jacobian = calculateJacobian(inConcreteStates, outConcreteStates, inDual.value)
        println("Jacobian has shape ${Arrays.toString(jacobian.shape)}")

        val values = DoubleTensor.create(doubleArrayOf(rhoS, rhoI, rhoR))
        val partialDerivatives = if (inDual.partialDerivatives.asMap().isEmpty()) {
            mapOf(inId to jacobian.matrixMultiply(DoubleTensor.create(doubleArrayOf(1.0, 1.0, 1.0)).transpose()))
        } else {
            inDual.partialDerivatives.asMap().mapValues {
                jacobian.matrixMultiply(it.value)
            }
        }

//        val partialDerivatives = inDual.partialDerivatives.asMap().mapValues {
//            jacobian.matrixMultiply(it.value)
//        }

        val dual = DualNumber(values, partialDerivatives)

//        println("Dual has ${values.length} values and ${partialDerivatives.size} partial derivatives of length ${partialDerivatives.values.first().length}")

        return dual
    }

    fun calculateJacobian(inConcreteStates: DoubleTensor, outConcreteStates: DoubleTensor, inDualValue: DoubleTensor): DoubleTensor {
        val jacobian =
            ((inConcreteStates * outConcreteStates).sum(1).matrixMultiply(inDualValue.reciprocal()) -
                outConcreteStates.sum(1).matrixMultiply(DoubleTensor.ones(1, 3))) / Nsamples.toDouble()

        return jacobian
    }

    fun setStateFromTensor(T: DoubleTensor) {
        rhoS = T.getValue(0)
        rhoI = T.getValue(1)
        rhoR = T.getValue(2)
    }

    fun setState(s: Double, i: Double, r: Double) {
        rhoS = s
        rhoI = i
        rhoR = r
    }

    fun getStateAsTensor(): DoubleTensor {
        return DoubleTensor.create(doubleArrayOf(rhoS, rhoI, rhoR))
    }

    fun getStateAsArray(): Array<DoubleTensor> {
        return arrayOf(DoubleTensor.scalar(rhoS), DoubleTensor.scalar(rhoI), DoubleTensor.scalar(rhoR))
    }

    fun setStateFromConcreteSamples() {
        rhoS = concreteStates.sumBy { model -> model.S } / Nsamples.toDouble()
        rhoI = concreteStates.sumBy { model -> model.I } / Nsamples.toDouble()
        rhoR = concreteStates.sumBy { model -> model.R } / Nsamples.toDouble()
    }

    fun asMatrix(samples: Array<SIRModel>): DoubleTensor {
        val s = DoubleTensor.zeros(intArrayOf(3, samples.size))
        for (i in 0 until samples.size) {
            s.setValue(samples[i].S.toDouble(), 0, i)
            s.setValue(samples[i].I.toDouble(), 1, i)
            s.setValue(samples[i].R.toDouble(), 2, i)
        }
        return s
    }

    fun createConcreteSamples(): Array<SIRModel> {
        return Array(Nsamples, {
            SIRModel(
                PoissonDistribution(rhoS).sample(),
                PoissonDistribution(rhoI).sample(),
                PoissonDistribution(rhoR).sample(),
                rand
            )
        })
    }
}