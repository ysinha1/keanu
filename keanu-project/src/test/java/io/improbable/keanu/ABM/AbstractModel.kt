package io.improbable.keanu.ABM

import io.improbable.keanu.research.Array2D
import io.improbable.keanu.tensor.intgr.IntegerTensor

class AbstractModel(val quadrantDimensions: Pair<Int, Int>, val quadrantArrangement: Pair<Int, Int>,
                    var quadrantPreyPopulation: IntArray, var quadrantPredatorPopulation: IntArray) {

    public enum class Agents { PREY, PREDATOR }

    val numberOfQuadrants = quadrantArrangement.first * quadrantArrangement.second
    val tensorShape = IntArray(2)
    var concreteStates = createConcreteSamples()
    val numberOfConcreteSamples = 1000

    init {
        if (quadrantPreyPopulation.size != numberOfQuadrants || quadrantPredatorPopulation.size != numberOfQuadrants) {
            throw IllegalArgumentException("You silly billy goat, the number of populations in the list must equal the number of quadrants")
        }
        tensorShape[0] = 2
        tensorShape[1] = numberOfQuadrants
    }

    fun step () {
        concreteStates = createConcreteSamples()
        // Todo complete
    }

    fun createConcreteSamples () : Array<Simulation> {
        return Array(numberOfConcreteSamples, {
            Simulation()
        }
    }

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

        // iterate through quadrants
        var quadrant_1 = Array2D<Agents>(quadrantDimensions.first * quadrantArrangement.first,
                                  quadrantDimensions.second * quadrantArrangement.second,
            {i, j ->
                if (flip is heads...)) {
                   // place prey
                }
            }
            // compute new flip probability and iterate through again to place the predators

            // combine into grid

        return grid
    }

    fun step (initialPopulationTensor: IntegerTensor): IntegerTensor {
        setStateFromTensor(initialPopulationTensor)
        step()
        return createTensorFromState()
    }

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