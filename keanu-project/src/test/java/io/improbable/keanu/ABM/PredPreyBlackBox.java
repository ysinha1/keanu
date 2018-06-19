package io.improbable.keanu.ABM;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.research.MixedInputOutputBlackBox;
import io.improbable.keanu.research.VertexBackedRandomFactory;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.PoissonVertex;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;

public class PredPreyBlackBox {

    public static Pair<IntegerTensor[], DoubleTensor[]> model(IntegerTensor[] integersIn, DoubleTensor[] doublesIn, RandomFactory<Double> random) {

        Simulation simulation = new Simulation(10, 10,
            new VertexBackedRandomFactory(10, 10),
            integersIn[2].scalar(), integersIn[0].scalar(), integersIn[1].scalar(),
            doublesIn[0].scalar(), doublesIn[1].scalar(), doublesIn[2].scalar());

        simulation.run();

        DoubleTensor[] doubleOutputs = new DoubleTensor[0];

        IntegerTensor[] integerOutputs = new IntegerTensor[2];
        int[] numberOfPredators = new int[1];
        numberOfPredators[0] = simulation.numberOfPredators;
        integerOutputs[0] = IntegerTensor.create(numberOfPredators);
        int[] numberOfPrey = new int[1];
        numberOfPrey[0] = simulation.numberOfPrey;
        integerOutputs[0] = IntegerTensor.create(numberOfPrey);

        return new Pair<>(integerOutputs, doubleOutputs);
    }

    public static void main (String[] args) {

        /*
        integers in:
        0 - Prey start population
        1 - Pred start population
        2 - Number of timesteps

        doubles in:
        0 - Prey reproduction gradient
        1 - Prey reproduction constant
        2 - Pred reproduction gradient

        integers out;
        0 - Prey start population
        1 - Pred start population
        */

        ArrayList<DoubleVertex> doublesIn = new ArrayList<>(3);
        doublesIn.add(new GaussianVertex(0.02, 0.1));
        doublesIn.add(new GaussianVertex(0.06, 0.1));
        doublesIn.add(new GaussianVertex(0.03, 0.1));

        ArrayList<IntegerVertex> integersIn = new ArrayList<>(3);
        integersIn.add(new PoissonVertex(20));
        integersIn.add(new PoissonVertex(5));
        integersIn.add(new PoissonVertex(100));

        MixedInputOutputBlackBox box = new MixedInputOutputBlackBox(integersIn, doublesIn, PredPreyBlackBox::model,
            2, 0);

        integersIn.get(0).observe(20);
        integersIn.get(1).observe(5);
        integersIn.get(2).observe(100);
        box.integerOutputs.get(0).observe(60);
        box.integerOutputs.get(1).observe(10);

        BayesianNetwork simulationNet = new BayesianNetwork(box.getConnectedGraph());
        ArrayList<Vertex> fromVertices = new ArrayList<>();
        fromVertices.addAll(doublesIn);
        fromVertices.addAll(integersIn);

        NetworkSamples samples = MetropolisHastings.getPosteriorSamples(simulationNet, fromVertices, 1000);
    }
}
