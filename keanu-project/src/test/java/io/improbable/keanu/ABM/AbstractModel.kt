package io.improbable.keanu.ABM

import io.improbable.keanu.research.Array2D
import io.improbable.keanu.tensor.intgr.IntegerTensor
import io.improbable.keanu.vertices.bool.probabilistic.Flip
import org.apache.commons.math3.distribution.PoissonDistribution

class AbstractModel(val quadrantDimensions: Pair<Int, Int>, val quadrantArrangement: Pair<Int, Int>,
                    var quadrantPreyPopulation: IntArray, var quadrantPredatorPopulation: IntArray) {

    public enum class Agents { PREY, PREDATOR, VACANT }

    val numberOfQuadrants = quadrantArrangement.first * quadrantArrangement.second
    val tensorShape = IntArray(2)
//    var concreteStates = createConcreteSamples()
    val numberOfConcreteSamples = 1000

    init {
        if (quadrantPreyPopulation.size != numberOfQuadrants || quadrantPredatorPopulation.size != numberOfQuadrants) {
            throw IllegalArgumentException("You silly billy goat, the number of populations in the list must equal the number of quadrants")
        }
        tensorShape[0] = 2
        tensorShape[1] = numberOfQuadrants
    }

    fun step () {
//        concreteStates = createConcreteSamples()
        // Todo complete
    }

//    fun createConcreteSamples () : Array<Simulation> {
//        return Array(numberOfConcreteSamples, {
//            Simulation()
//        }
//    }

    fun createGridSample () : Array2D<Agents> {

        // TODO 18-JULY
        // For each quadrant, we iterate through once for pred then once more for prey
        // For each square in the quadrant we flip for the presence of a prey based on the number of squares in the
        // quadrant and a desired population which is a poisson draw of the abstract model's population in that quadrant
        // On the second run through the flip probability of a predator is assigned the same way but using the remaining
        // *blank* squares.
        // Then assemble the quadrants into a grid to be passed to the simulation.
        // Moderate the simulation to accept a grid of the appropriate typing - agents must be initialised AFTER the
        // simulation because the Agent supertype requires knowledge of the simulation. This is potentially poor form
        // and may be should be fixed.

        var quadrants = Array<Array2D<Agents>>(numberOfQuadrants, { quadrantNumber ->
            return Array2D(quadrantDimensions.first * quadrantArrangement.first,
                           quadrantDimensions.second * quadrantArrangement.second,
                { i, j ->
                    var targetPreyPopulationToSpawn = PoissonDistribution(quadrantPreyPopulation[quadrantNumber].toDouble()).sample().toDouble()
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
            for (i in 0..quadrant.iSize()) {
                for (j in 0..quadrant.jSize()) {
                    if (quadrant[i, j] == Agents.VACANT) {
                        nullCounter += 1
                    }
                }
            }
            var targetPredatorPopulationToSpawn = PoissonDistribution(quadrantPredatorPopulation[quadrantNumber].toDouble()).sample().toDouble()
            var probabilityPerGridSquare = targetPredatorPopulationToSpawn / nullCounter
            for (i in 0..quadrant.iSize()) {
                for (j in 0..quadrant.jSize()) {
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
        for (j in 0..quadrantArrangement.second) {
            for (i in 0 until quadrantArrangement.first) {
                var gridRow = quadrants[i].horizontallyStack(quadrants[i+1])
            }
            quadrantRows[j] = gridRow
        }
        var grid = quadrantRows[0]
        for (i in 1 until quadrantArrangement.second) {
            grid = grid.verticallyStack(quadrantRows[i])
        }

        return grid
    }

//    fun step (initialPopulationTensor: IntegerTensor): IntegerTensor {
//        setStateFromTensor(initialPopulationTensor)
//        step()
//        return createTensorFromState()
//    }

    fun setStateFromTensor (T: IntegerTensor) {
        if (T.rank != 2 && T.shape[0] != 2 && T.shape[1] != numberOfQuadrants) {
            throw IllegalArgumentException("The initial population tensor is the wrong rank or shape for the number of quadrants and agent types")
        }
        for (i in 0 .. numberOfQuadrants) {
            quadrantPreyPopulation[i] = T.getValue(0, i)
            quadrantPredatorPopulation[i] = T.getValue(0, i)
        }
    }

    fun createTensorFromState (quadrantPreyPopulation: IntArray, quadrantPredatorPopulation: IntArray): IntegerTensor {
        var T = IntegerTensor.create(tensorShape)
        for (i in 0 .. numberOfQuadrants) {
            T.setValue(quadrantPreyPopulation[i], 0, i)
            T.setValue(quadrantPredatorPopulation[i], 1, i)
        }
        return T
    }


}