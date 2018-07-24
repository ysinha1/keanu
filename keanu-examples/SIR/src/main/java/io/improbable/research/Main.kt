package io.improbable.research

import io.improbable.keanu.algorithms.variational.GradientOptimizer
import io.improbable.keanu.network.BayesianNetwork
import io.improbable.keanu.research.vertices.DoubleTensorArrayIndexingVertex
import io.improbable.keanu.tensor.dbl.DoubleTensor
import io.improbable.keanu.vertices.dbl.DoubleVertex
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex
import org.apache.commons.math3.random.MersenneTwister
import java.io.FileWriter


fun main(args: Array<String>) {
    assimilateData()

//    testAbstractDiffS()
//    testAbstractDiffI()
//    testAbstractDiffR()

//    testTensorSplit()
}



fun assimilateData(): Array<TimestepState> {
    println("Running concrete model")
    val concreteTimestepStates = runConcrete()
    println("Getting observations")
    val observations = getObservations(concreteTimestepStates)
    println("Running probabilistic data assimilation")

    var state: DoubleVertex = initializeState(960.0, 40.0, 1.00)

    val sTimestepStates = arrayListOf<DoubleVertex>()
    val iTimestepStates = arrayListOf<DoubleVertex>()
    val rTimestepStates = arrayListOf<DoubleVertex>()

    for (obs in observations) {
        state = ModelVertex(state)
        val s = DoubleTensorArrayIndexingVertex(state, intArrayOf(0, 1), intArrayOf(0, 1))
        val i = DoubleTensorArrayIndexingVertex(state, intArrayOf(0, 2), intArrayOf(0, 2))
        val r = DoubleTensorArrayIndexingVertex(state, intArrayOf(0, 2), intArrayOf(0, 2))
        GaussianVertex(i, 1.0).observe(obs)
        sTimestepStates.add(s)
        iTimestepStates.add(i)
        rTimestepStates.add(r)
    }

    val bayesianNetwork = BayesianNetwork(state.connectedGraph)
    println("Bayesian network has ${state.connectedGraph.size} vertices")

    val gradientOptimizer = GradientOptimizer(bayesianNetwork)
    gradientOptimizer.maxAPosteriori(10000)

    return Array<TimestepState>(observations.size, { i: Int ->
        val S = sTimestepStates[i].value.scalar()
        val I = iTimestepStates[i].value.scalar()
        val R = rTimestepStates[i].value.scalar()
        TimestepState(S, I, R)
    })
}

fun runAbstract() {
    val file: FileWriter? = null
    //file = FileWriter("data.out")
    val model = AbstractModel(96.0, 4.0, 0.01)
    for (step in 1..40) {
        model.step()
        val out = "$step ${model.rhoS} ${model.rhoI} ${model.rhoR}\n"
        file?.write(out)
        print(out)
    }
    file?.close()
}

fun runConcrete(): Array<TimestepState> {
    val STEPS = 40
//    val file = FileWriter("data.out")
    val model = SIRModel(96, 4, 0, MersenneTwister())
    val timestepStates = Array(STEPS, { i ->
        model.step()
//        file.write("$step ${model.S} ${model.I} ${model.R}\n")
//        println("$step ${model.S} ${model.I} ${model.R}")
        TimestepState(model.S.toDouble(), model.I.toDouble(), model.R.toDouble())
    })

//    file.close()
    return timestepStates
}

fun getObservations(timestepStates: Array<TimestepState>): DoubleArray {
    return timestepStates.map { ts -> ts.i }.toDoubleArray()
}

fun testTensorSplit() {
    val di = 1.0
    val s = 960.0
    val i = 40.0
    val r = 5.0

    var state = initializeState(s, i, r)
    val model = ModelVertex(state)
    val dual = model.dualNumber
    val jacobian = dual.partialDerivatives.withRespectTo(state)

    val model2 = AbstractModel(s, i + di, r)
    model2.step()

    val extracted = DoubleTensorArrayIndexingVertex(model, intArrayOf(0, 1), intArrayOf(0, 1))
    val extractedJacobian = extracted.dualNumber.partialDerivatives.withRespectTo(state)

    println("Jacobian is:")
    println(jacobian)

    println("Extracted value = " + extracted.value.scalar())
    println("Extracted Jacobian is:")
    println(extractedJacobian)
}

fun testAbstractDiffS() {
    println("Testing abstract diff with respect to S")

    val ds = 1.0
    val s = 960.0
    val i = 40.0
    val r = 5.0

    var state = initializeState(s, i, r)
    val model1 = AbstractModel(s, i, r)
    val dual = model1.calculateDualNumber(state.dualNumber)!!
    val jacobian = dual.partialDerivatives.withRespectTo(state)

    val model2 = AbstractModel(s + ds, i, r)
    model2.step()

    val ds1_ds0 = (model2.rhoS - model1.rhoS) / ds
    val di1_ds0 = (model2.rhoI - model1.rhoI) / ds
    val dr1_ds0 = (model2.rhoR - model1.rhoR) / ds

    println("Jacobian is:")
    println(jacobian)

    val dSout_dSin = jacobian.getValue(0, 0, 0, 0)
    val dSout_dIin = jacobian.getValue(0, 0, 0, 1)
    val dSout_dRin = jacobian.getValue(0, 0, 0, 2)
    val dIout_dSin = jacobian.getValue(0, 1, 0, 0)
    val dIout_dIin = jacobian.getValue(0, 1, 0, 1)
    val dIout_dRin = jacobian.getValue(0, 1, 0, 2)
    val dRout_dSin = jacobian.getValue(0, 2, 0, 0)
    val dRout_dIin = jacobian.getValue(0, 2, 0, 1)
    val dRout_dRin = jacobian.getValue(0, 2, 0, 2)

    println("ds1_ds0 finite = $ds1_ds0, calculated = $dSout_dSin")
    println("di1_ds0 finite = $di1_ds0, calculated = $dIout_dSin")
    println("dr1_ds0 finite = $dr1_ds0, calculated = $dRout_dSin")
    println("dSout_dSin = $dSout_dSin")
    println("dSout_dIin = $dSout_dIin")
    println("dSout_dRin = $dSout_dRin")
    println("dIout_dSin = $dIout_dSin")
    println("dIout_dIin = $dIout_dIin")
    println("dIout_dRin = $dIout_dRin")
    println("dRout_dSin = $dRout_dSin")
    println("dRout_dIin = $dRout_dIin")
    println("dRout_dRin = $dRout_dRin")
    println()
}

fun testAbstractDiffI() {
    println("Testing abstract diff with respect to I")

    val di = 1.0
    val s = 960.0
    val i = 40.0
    val r = 5.0

    var state = initializeState(s, i, r)
    val model1 = AbstractModel(s, i, r)
    val dual = model1.calculateDualNumber(state.dualNumber)!!
    val jacobian = dual.partialDerivatives.withRespectTo(state)

    val model2 = AbstractModel(s, i + di, r)
    model2.step()

    val ds1_di0 = (model2.rhoS - model1.rhoS) / di
    val di1_di0 = (model2.rhoI - model1.rhoI) / di
    val dr1_di0 = (model2.rhoR - model1.rhoR) / di

    println("Jacobian is:")
    println(jacobian)

    val dSout_dSin = jacobian.getValue(0, 0, 0, 0)
    val dSout_dIin = jacobian.getValue(0, 0, 0, 1)
    val dSout_dRin = jacobian.getValue(0, 0, 0, 2)
    val dIout_dSin = jacobian.getValue(0, 1, 0, 0)
    val dIout_dIin = jacobian.getValue(0, 1, 0, 1)
    val dIout_dRin = jacobian.getValue(0, 1, 0, 2)
    val dRout_dSin = jacobian.getValue(0, 2, 0, 0)
    val dRout_dIin = jacobian.getValue(0, 2, 0, 1)
    val dRout_dRin = jacobian.getValue(0, 2, 0, 2)

    println("ds1_di0 finite = $ds1_di0, calculated = $dSout_dIin")
    println("di1_di0 finite = $di1_di0, calculated = $dIout_dIin")
    println("dr1_di0 finite = $dr1_di0, calculated = $dRout_dIin")
    println("dSout_dSin = $dSout_dSin")
    println("dSout_dIin = $dSout_dIin")
    println("dSout_dRin = $dSout_dRin")
    println("dIout_dSin = $dIout_dSin")
    println("dIout_dIin = $dIout_dIin")
    println("dIout_dRin = $dIout_dRin")
    println("dRout_dSin = $dRout_dSin")
    println("dRout_dIin = $dRout_dIin")
    println("dRout_dRin = $dRout_dRin")
    println()
}

fun testAbstractDiffR() {
    println("Testing abstract diff with respect to R")

    val dr = 1.0
    val s = 960.0
    val i = 40.0
    val r = 5.0

    var state = initializeState(s, i, r)
    val model1 = AbstractModel(s, i, r)
    val dual = model1.calculateDualNumber(state.dualNumber)!!
    val jacobian = dual.partialDerivatives.withRespectTo(state)

    val model2 = AbstractModel(s, i, r + dr)
    model2.step()

    val ds1_dr0 = (model2.rhoS - model1.rhoS) / dr
    val di1_dr0 = (model2.rhoI - model1.rhoI) / dr
    val dr1_dr0 = (model2.rhoR - model1.rhoR) / dr

    println("Jacobian is:")
    println(jacobian)

    val dSout_dSin = jacobian.getValue(0, 0, 0, 0)
    val dSout_dIin = jacobian.getValue(0, 0, 0, 1)
    val dSout_dRin = jacobian.getValue(0, 0, 0, 2)
    val dIout_dSin = jacobian.getValue(0, 1, 0, 0)
    val dIout_dIin = jacobian.getValue(0, 1, 0, 1)
    val dIout_dRin = jacobian.getValue(0, 1, 0, 2)
    val dRout_dSin = jacobian.getValue(0, 2, 0, 0)
    val dRout_dIin = jacobian.getValue(0, 2, 0, 1)
    val dRout_dRin = jacobian.getValue(0, 2, 0, 2)

    println("ds1_dr0 finite = $ds1_dr0, calculated = $dSout_dRin")
    println("di1_dr0 finite = $di1_dr0, calculated = $dIout_dRin")
    println("dr1_dr0 finite = $dr1_dr0, calculated = $dRout_dRin")
    println("dSout_dSin = $dSout_dSin")
    println("dSout_dIin = $dSout_dIin")
    println("dSout_dRin = $dSout_dRin")
    println("dIout_dSin = $dIout_dSin")
    println("dIout_dIin = $dIout_dIin")
    println("dIout_dRin = $dIout_dRin")
    println("dRout_dSin = $dRout_dSin")
    println("dRout_dIin = $dRout_dIin")
    println("dRout_dRin = $dRout_dRin")
    println()
}


fun testConcreteDiff() {
    val S = 960
    val I = 40
    val R = 0
    val rand = MersenneTwister()
    var ds1_di0 = 0.0
    var di1_di0 = 0.0
    var dr1_di0 = 0.0
    val Nsamples = 1000000

    for (i in 1..Nsamples) {
        val model1 = SIRModel(S, I, R, rand)
        val model2 = SIRModel(S, I + 1, R, rand)
        model1.step()
        model2.step()
        ds1_di0 += (model2.S - model1.S) / (1.0 * Nsamples)
        di1_di0 += (model2.I - model1.I) / (1.0 * Nsamples)
        dr1_di0 += (model2.R - model1.R) / (1.0 * Nsamples)
    }

    println("$ds1_di0 $di1_di0 $dr1_di0")
}

fun testAbstractFiniteDiff() {
    val S = 960.0
    val I = 40.0
    val R = 0.01
    val di = 0.5

    val model1 = AbstractModel(S, I, R)
    val model2 = AbstractModel(S, I + di, R)
    model1.step()
    model2.step()
    val ds1_di0 = (model2.rhoS - model1.rhoS) / di
    val di1_di0 = (model2.rhoI - model1.rhoI) / di
    val dr1_di0 = (model2.rhoR - model1.rhoR) / di

    println("$ds1_di0 $di1_di0 $dr1_di0")
}


fun initializeState(s: Double, i: Double, r: Double): DoubleVertex {
    val mu = ConstantDoubleVertex(doubleArrayOf(s, i, r))
    val sigma = ConstantDoubleVertex(doubleArrayOf(1.0, 1.0, 1.0))
    val g = GaussianVertex(intArrayOf(1, 3), mu, sigma)
    g.setValue(doubleArrayOf(s, i, r))
    return g
}

