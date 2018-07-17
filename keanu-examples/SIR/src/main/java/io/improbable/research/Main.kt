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
    println("Running concrete model")
    val concreteTimestepStates = runConcrete()
    println("Getting observations")
    val observations = getObservations(concreteTimestepStates)
    println("Running probabilistic data assimilation")
    val probabilisticTimestepStates = assimilateData(observations)
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