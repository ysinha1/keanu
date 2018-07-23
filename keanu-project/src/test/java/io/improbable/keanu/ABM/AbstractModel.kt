package io.improbable.keanu.ABM

import io.improbable.keanu.research.Array2D
import io.improbable.keanu.tensor.dbl.DoubleTensor
import io.improbable.keanu.vertices.bool.probabilistic.Flip
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber
import org.apache.commons.math3.distribution.PoissonDistribution

class AbstractModel(val quadrantDimensions: Pair<Int, Int>, val quadrantArrangement: Pair<Int, Int>,
                    var quadrantPreyPopulationLambda: DoubleArray, var quadrantPredatorPopulationLambda: DoubleArray) {

    public enum class Agents { PREY, PREDATOR, VACANT }

    val numberOfQuadrants = quadrantArrangement.first * quadrantArrangement.second
    val tensorShape = IntArray(2)
    var hasBeenCalled = 0
    val numberOfConcreteSamples = 1000

    init {
        if (quadrantPreyPopulationLambda.size != numberOfQuadrants || quadrantPredatorPopulationLambda.size != numberOfQuadrants) {
            throw IllegalArgumentException("You silly billy goat, the number of populations in the list must equal the number of quadrants")
        }
        tensorShape[0] = 2
        tensorShape[1] = numberOfQuadrants
    }

    fun step (initialPopulationTensor: DoubleTensor): DoubleTensor {
        setStateFromTensor(initialPopulationTensor)
        step()
        return createTensorFromState()
    }

    fun step () {
        var concreteStates = createConcreteSamples()
        concreteStates.forEach { model -> model.step() }
        setStateFromConcreteSamples(concreteStates)
    }

    fun createConcreteSamples () : Array<Simulation> {
        return Array(numberOfConcreteSamples, {
            Simulation(createConcreteSampleStartGrid(), 0.02,0.06, 0.03)
        })
    }

    fun calculateDualNumber (inDual: DualNumber?): DualNumber? {
        hasBeenCalled++
        if (inDual == null) return null

        setStateFromTensor(inDual.value)
        val concreteStates = createConcreteSamples()
        val inConcreteStates = concreteStatesAsTensor(concreteStates)
        concreteStates.forEach { it.step() }
        setStateFromConcreteSamples(concreteStates)
        val outConcreteStates = concreteStatesAsTensor(concreteStates)
        val jacobian = calculateJacobianElementwise(inConcreteStates, outConcreteStates, inDual.value)
        val values = DoubleTensor.create(doubleArrayOf( // todo insert state doubles
             ))
        val partialDerivatives = inDual.partialDerivatives.asMap().mapValues {
            jacobian.tensorMultiply(it.value, intArrayOf(1), intArrayOf(1)).reshape(
                // todo
            )}
        return DualNumber(values, partialDerivatives)

    }

    fun calculateJacobianElementwise(inConcreteSamples: DoubleTensor, outConcreteSamples: DoubleTensor,
                                     inAbstractState: DoubleTensor) : DoubleTensor {

    }

    fun concreteStatesAsTensor (samples: Array<Simulation>) : DoubleTensor {
        // TODO
    }

    fun createConcreteSampleStartGrid () : Array2D<Agents> {
        var quadrants = Array<Array2D<Agents>>(numberOfQuadrants, { quadrantNumber ->
            return Array2D(quadrantDimensions.first * quadrantArrangement.first,
                           quadrantDimensions.second * quadrantArrangement.second,
                { i, j ->
                    var targetPreyPopulationToSpawn = PoissonDistribution(quadrantPreyPopulationLambda[quadrantNumber].toDouble()).sample().toDouble()
                    var probabilityPerGridSquare = targetPreyPopulationToSpawn / (quadrantDimensions.first * quadrantDimensions.second)
                    if (Flip(probabilityPerGridSquare).sampleUsingDefaultRandom().scalar()) {
                        return@Array2D Agents.PREY
                    } else {
                        return@Array2D Agents.VACANT
                    }
                })
        })
        var quadrantNumber = 0
        for (quadrant in quadrants) {
            var nullCounter = 0
            for (i in 0 until quadrant.iSize()) {
                for (j in 0 until quadrant.jSize()) {
                    if (quadrant[i, j] == Agents.VACANT) {
                        nullCounter += 1
                    }
                }
            }
            var targetPredatorPopulationToSpawn = PoissonDistribution(quadrantPredatorPopulationLambda[quadrantNumber].toDouble()).sample().toDouble()
            var probabilityPerGridSquare = targetPredatorPopulationToSpawn / nullCounter
            for (i in 0 until quadrant.iSize()) {
                for (j in 0 until quadrant.jSize()) {
                    if (quadrant[i, j] == Agents.VACANT) {
                        if (Flip(probabilityPerGridSquare).sampleUsingDefaultRandom().scalar()) {
                            quadrant[i, j] = Agents.PREDATOR
                        }
                    }
                }
            }
            quadrantNumber += 1
        }

        var quadrantRows = Array(quadrantArrangement.second, {_ -> Array2D(1, 1, {_, _ -> Agents.VACANT})})
        var gridRow = quadrants[0]
        for (j in 0 until quadrantArrangement.second) {
            for (i in 1 until quadrantArrangement.first) {
                var gridRow = gridRow.horizontallyStack(quadrants[i])
            }
            quadrantRows[j] = gridRow
        }
        var grid = quadrantRows[0]
        for (i in 1 until quadrantArrangement.second) {
            grid = grid.verticallyStack(quadrantRows[i])
        }

        return grid
    }

    fun mapConcreteSampleToAbstractSpace (concreteState: Simulation) : Pair<IntArray, IntArray> {
        var abstractState = Pair(IntArray(numberOfQuadrants), IntArray(numberOfQuadrants))
        for (quadCol in 0 until quadrantArrangement.first) {
            for (quadRow in 0 until quadrantArrangement.second) {
                var quadIndex = quadCol + quadRow * quadrantArrangement.first
                var offset = Pair(quadCol*quadrantDimensions.first, quadRow*quadrantDimensions.second)
                for (col in 0 until quadrantDimensions.first) {
                    for (row in 0 until quadrantDimensions.second) {
                        var candidate = concreteState.getXY(col + offset.first, col + offset.second)
                        if (candidate is Prey) {
                            abstractState.first[quadIndex] +=1
                        } else if (candidate is Predator) {
                            abstractState.second[quadIndex] += 1
                        }
                    }
                }
            }
        }
        return abstractState
    }

    fun setStateFromTensor (T: DoubleTensor) {
        if (T.rank != 2 && T.shape[0] != 2 && T.shape[1] != numberOfQuadrants) {
            throw IllegalArgumentException("The initial population tensor is the wrong rank or shape for the number of quadrants and agent types")
        }
        for (i in 0 until numberOfQuadrants) {
            quadrantPreyPopulationLambda[i] = T.getValue(0, i)
            quadrantPredatorPopulationLambda[i] = T.getValue(0, i)
        }
    }

    fun setStateFromConcreteSamples (samples: Array<Simulation>) {
        var abstractSamples = Array(samples.size, {i ->
            mapConcreteSampleToAbstractSpace(samples[i])
        })
        var sum = Pair(DoubleArray(numberOfQuadrants), DoubleArray(numberOfQuadrants))
        for (sample in abstractSamples) {
            for (quadrant in 0 until numberOfQuadrants) {
                sum.first[quadrant] = sum.first[quadrant] + sample.first[quadrant]
                sum.second[quadrant] = sum.second[quadrant] + sample.second[quadrant]
            }
        }
        for (quadrant in 0 until numberOfQuadrants) {
            quadrantPreyPopulationLambda[quadrant] = sum.first[quadrant] / numberOfConcreteSamples
            quadrantPredatorPopulationLambda[quadrant] = sum.second[quadrant] / numberOfConcreteSamples
        }
    }

    fun createTensorFromState (): DoubleTensor {
        var T = DoubleTensor.create(0.0, tensorShape)
        for (i in 0 until numberOfQuadrants) {
            T.setValue(quadrantPreyPopulationLambda[i], 0, i)
            T.setValue(quadrantPredatorPopulationLambda[i], 1, i)
        }
        return T
    }


}