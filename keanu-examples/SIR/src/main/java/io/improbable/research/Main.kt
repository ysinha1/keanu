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


fun main(args : Array<String>) {
//    println("Running concrete model")
//    val concreteTimestepStates = runConcrete()
//    println("Getting observations")
//    val observations = getObservations(concreteTimestepStates)
//    println("Running probabilistic data assimilation")
//    val probabilisticTimestepStates = assimilateData(observations)

    testAbstractDiff()
}

fun assimilateData(observations : DoubleArray): Array<TimestepState> {
    var state : DoubleVertex = ConstantDoubleVertex(DoubleTensor.create(doubleArrayOf(96.0, 4.0, 0.01)))

    val sTimestepStates = arrayListOf<DoubleVertex>()
    val iTimestepStates = arrayListOf<DoubleVertex>()
    val rTimestepStates = arrayListOf<DoubleVertex>()

    for(obs in observations) {
//        val modelTimestep = ModelVertex(state)
        state = ModelVertex(state)
        val s = DoubleTensorArrayIndexingVertex(state, 0)
        val i = DoubleTensorArrayIndexingVertex(state, 1)
        val r = DoubleTensorArrayIndexingVertex(state, 2)
        GaussianVertex(i, 0.5).observe(obs)
        sTimestepStates.add(s)
        iTimestepStates.add(i)
        rTimestepStates.add(r)
//        state = modelTimestep
    }

    val bayesianNetwork = BayesianNetwork(state.connectedGraph)
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
    val file : FileWriter? = null
    //file = FileWriter("data.out")
    val model = AbstractModel(96.0, 4.0, 0.01)
    for(step in 1..40) {
        model.step()
        val out = "$step ${model.rhoS} ${model.rhoI} ${model.rhoR}\n"
        file?.write(out)
        print(out)
    }
    file?.close()
}

fun runConcrete() : Array<TimestepState> {
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

fun testAbstractDiff() {
    val deltai = 0.5
    val s = 960.0
    val i = 40.0
    val r = 5.0

//    val model = AbstractModel(s,i,r)
//    model.step()
//    val model1 = AbstractModel(s, i+deltai,r)
//    model1.step()
//
//    var ds = (model1.rhoS - model.rhoS)/deltai
//    var di = (model1.rhoI - model.rhoI)/deltai
//    var dr = (model1.rhoR - model.rhoR)/deltai
//
//    println("ds'/di $ds, di'/di $di, dr'/di $dr")

    var state = initializeState(s, i, r)
    val model2 = AbstractModel(s,i,r)
    val dual = model2.calculateDualNumber(state.dualNumber)!!
    val jacobian = dual.partialDerivatives.withRespectTo(state)

    println("Jacobian is:")
    println(jacobian)

//    var state2 : DoubleVertex = ConstantDoubleVertex(DoubleTensor.create(doubleArrayOf(96.1, 4.0, 0.01)))
//    val model2 = AbstractModel(state.value)
//    val dual2 = model.calculateDualNumber(state.dualNumber, state.id)!!
//    println(dual2.value)
//    println(dual2.partialDerivatives.withRespectTo(state))
//
//    GaussianVertex(intArrayOf(1, 3), 0.0, 1.0)
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

    for(i in 1..Nsamples) {
        val model1 = SIRModel(S,I,R,rand)
        val model2 = SIRModel(S,I+1,R,rand)
        model1.step()
        model2.step()
        ds1_di0 += (model2.S-model1.S)/(1.0*Nsamples)
        di1_di0 += (model2.I-model1.I)/(1.0*Nsamples)
        dr1_di0 += (model2.R-model1.R)/(1.0*Nsamples)
    }

    println("$ds1_di0 $di1_di0 $dr1_di0")
}

fun testAbstractFiniteDiff() {
    val S = 960.0
    val I = 40.0
    val R = 0.01
    val di = 0.5

    val model1 = AbstractModel(S,I,R)
    val model2 = AbstractModel(S,I+di,R)
    model1.step()
    model2.step()
    val ds1_di0 = (model2.rhoS-model1.rhoS)/di
    val di1_di0 = (model2.rhoI-model1.rhoI)/di
    val dr1_di0 = (model2.rhoR-model1.rhoR)/di

    println("$ds1_di0 $di1_di0 $dr1_di0")
}


fun initializeState(s: Double, i: Double, r: Double): DoubleVertex {
    val g = GaussianVertex(intArrayOf(1,3),0.0,1.0)
    g.setValue(doubleArrayOf(s, i, r))
    return g
}

