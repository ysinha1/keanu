package io.improbable.keanu.ABM

import io.improbable.keanu.tensor.dbl.DoubleTensor

fun main(args: Array<String>) {
    var nSamples = 5

    var testTensor = DoubleTensor.zeros(intArrayOf(3, nSamples)).reshape(3, 1, nSamples)
    var foilTensor = DoubleTensor.zeros(intArrayOf(3, nSamples)).reshape(1, 3, nSamples)
    var urg = testTensor.tensorMultiply(foilTensor, intArrayOf(1), intArrayOf(0))

    for (i in 0 until urg.shape.size) {
        println(urg.shape[i])
    }
}