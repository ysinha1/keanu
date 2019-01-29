package io.improbable.keanu.benchmarks;

import io.improbable.keanu.algorithms.variational.optimizer.KeanuProbabilisticWithGradientGraph;
import io.improbable.keanu.algorithms.variational.optimizer.OptimizedResult;
import io.improbable.keanu.algorithms.variational.optimizer.Optimizer;
import io.improbable.keanu.algorithms.variational.optimizer.gradient.Adam;
import io.improbable.keanu.algorithms.variational.optimizer.gradient.GradientOptimizer;
import io.improbable.keanu.algorithms.variational.optimizer.nongradient.NonGradientOptimizer;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.util.status.StatusBar;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
public class OptimizerBenchmark {

    public enum OptimizerType {
        ADAM, APACHE_GRADIENT, APACHE_NON_GRADIENT
    }

    @Param({"ADAM", "APACHE_GRADIENT", "APACHE_NON_GRADIENT"})
    public OptimizerType optimizerType;

    private Optimizer optimizer;

    @Setup
    public void setup() {

        StatusBar.disable();

        GaussianVertex A = new GaussianVertex(10, 0.1);
        A.setValue(0.0);
        GaussianVertex B = new GaussianVertex(5, 1);
        B.setValue(0.0);
        GaussianVertex C = new GaussianVertex(A.times(B), 0.1);
        C.observe(30.0);

        BayesianNetwork bayesianNetwork = new BayesianNetwork(A.getConnectedGraph());

        switch (optimizerType) {
            case ADAM:
                optimizer = GradientOptimizer.builder()
                    .bayesianNetwork(new KeanuProbabilisticWithGradientGraph(bayesianNetwork))
                    .algorithm(Adam.builder().build()).build();
                break;

            case APACHE_GRADIENT:
                optimizer = GradientOptimizer.builder()
                    .bayesianNetwork(new KeanuProbabilisticWithGradientGraph(bayesianNetwork))
                    .build();
                break;
            case APACHE_NON_GRADIENT:
                optimizer = NonGradientOptimizer.builder()
                    .bayesianNetwork(new KeanuProbabilisticWithGradientGraph(bayesianNetwork))
                    .build();
                break;
        }
    }

    @Benchmark
    public OptimizedResult baseline() {
        return optimizer.maxAPosteriori();
    }

}
