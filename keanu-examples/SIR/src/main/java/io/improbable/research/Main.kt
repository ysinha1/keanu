package io.improbable.research

import io.improbable.keanu.research.vertices.DoubleArrayIndexingVertex
import io.improbable.keanu.tensor.dbl.DoubleTensor
import io.improbable.keanu.vertices.ConstantVertex
import io.improbable.keanu.vertices.dbl.DoubleVertex
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex
import org.apache.commons.math3.random.MersenneTwister
import java.io.FileWriter

fun main(args : Array<String>) {
    val observations = runConcrete();
    dataAssimilate(observations)
}

fun dataAssimilate(observations : DoubleArray) {
    var state : DoubleVertex = ConstantDoubleVertex(DoubleTensor.create(doubleArrayOf(96.0, 4.0, 0.01)))
    for(obs in observations) {
        state = ModelVertex(state)
//        val nSuseptible = DoubleArrayIndexingVertex

    }

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

fun runConcrete() : DoubleArray {
    val STEPS = 40
//    val file = FileWriter("data.out")
    val model = SIRModel(96, 4, 0, MersenneTwister())
    val observations = DoubleArray(STEPS)
    for(step in 0 until STEPS) {
        model.step()
        observations[step] = model.S.toDouble()
//        file.write("$step ${model.S} ${model.I} ${model.R}\n")
//        println("$step ${model.S} ${model.I} ${model.R}")
    }
//    file.close()
    return observations
}