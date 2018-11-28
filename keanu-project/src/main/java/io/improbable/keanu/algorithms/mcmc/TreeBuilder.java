package io.improbable.keanu.algorithms.mcmc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.ProbabilityCalculator;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexId;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.LogProbGradientCalculator;

public class TreeBuilder {

    private static final double DELTA_MAX = 1000.0;

    Leapfrog leapForward;
    Leapfrog leapBackward;
    Map<VertexId, DoubleTensor> acceptedPosition;
    Map<VertexId, DoubleTensor> gradientAtAcceptedPosition;
    double logOfMasterPAtAcceptedPosition;
    Map<VertexId, ?> sampleAtAcceptedPosition;
    int acceptedLeapfrogCount;
    boolean shouldContinueFlag;
    double deltaLikelihoodOfLeapfrog;
    double treeSize;

    TreeBuilder(Leapfrog leapForward,
                Leapfrog leapBackward,
                Map<VertexId, DoubleTensor> acceptedPosition,
                Map<VertexId, DoubleTensor> gradientAtAcceptedPosition,
                double logProbAtAcceptedPosition,
                Map<VertexId, ?> sampleAtAcceptedPosition,
                int acceptedLeapfrogCount,
                boolean shouldContinueFlag,
                double deltaLikelihoodOfLeapfrog,
                double treeSize) {

        this.leapForward = leapForward;
        this.leapBackward = leapBackward;
        this.acceptedPosition = acceptedPosition;
        this.gradientAtAcceptedPosition = gradientAtAcceptedPosition;
        this.logOfMasterPAtAcceptedPosition = logProbAtAcceptedPosition;
        this.sampleAtAcceptedPosition = sampleAtAcceptedPosition;
        this.acceptedLeapfrogCount = acceptedLeapfrogCount;
        this.shouldContinueFlag = shouldContinueFlag;
        this.deltaLikelihoodOfLeapfrog = deltaLikelihoodOfLeapfrog;
        this.treeSize = treeSize;
    }

    public TreeBuilder buildOtherHalfOfTree(TreeBuilder currentTree,
                                                    List<Vertex<DoubleTensor>> latentVertices,
                                                    List<Vertex> probabilisticVertices,
                                                    LogProbGradientCalculator logProbGradientCalculator,
                                                    final List<? extends Vertex> sampleFromVertices,
                                                    double u,
                                                    int buildDirection,
                                                    int treeHeight,
                                                    double epsilon,
                                                    double logOfMasterPMinusMomentumBeforeLeapfrog,
                                                    KeanuRandom random) {

        TreeBuilder otherHalfTree;

        if (buildDirection == -1) {

            otherHalfTree = buildTree(
                latentVertices,
                probabilisticVertices,
                logProbGradientCalculator,
                sampleFromVertices,
                currentTree.leapBackward,
                u,
                buildDirection,
                treeHeight,
                epsilon,
                logOfMasterPMinusMomentumBeforeLeapfrog,
                random
            );

            currentTree.leapBackward = otherHalfTree.leapBackward;

        } else {

            otherHalfTree = buildTree(
                latentVertices,
                probabilisticVertices,
                logProbGradientCalculator,
                sampleFromVertices,
                currentTree.leapForward,
                u,
                buildDirection,
                treeHeight,
                epsilon,
                logOfMasterPMinusMomentumBeforeLeapfrog,
                random
            );

            currentTree.leapForward = otherHalfTree.leapForward;
        }

        return otherHalfTree;
    }

    public TreeBuilder buildTree(List<Vertex<DoubleTensor>> latentVertices,
                                         List<Vertex> probabilisticVertices,
                                         LogProbGradientCalculator logProbGradientCalculator,
                                         final List<? extends Vertex> sampleFromVertices,
                                         Leapfrog leapfrog,
                                         double u,
                                         int buildDirection,
                                         int treeHeight,
                                         double epsilon,
                                         double logOfMasterPMinusMomentumBeforeLeapfrog,
                                         KeanuRandom random) {
        if (treeHeight == 0) {

            //Base case-take one leapfrog step in the build direction

            return TreeBuilderBaseCase(latentVertices,
                probabilisticVertices,
                logProbGradientCalculator,
                sampleFromVertices,
                leapfrog,
                u,
                buildDirection,
                epsilon,
                logOfMasterPMinusMomentumBeforeLeapfrog
            );

        } else {
            //Recursion-implicitly build the left and right subtrees.

            TreeBuilder tree = buildTree(
                latentVertices,
                probabilisticVertices,
                logProbGradientCalculator,
                sampleFromVertices,
                leapfrog,
                u,
                buildDirection,
                treeHeight - 1,
                epsilon,
                logOfMasterPMinusMomentumBeforeLeapfrog,
                random
            );

            //Should continue building other half if first half's shouldContinueFlag is true
            if (tree.shouldContinueFlag) {

                TreeBuilder otherHalfTree = buildOtherHalfOfTree(
                    tree,
                    latentVertices,
                    probabilisticVertices,
                    logProbGradientCalculator,
                    sampleFromVertices,
                    u,
                    buildDirection,
                    treeHeight - 1,
                    epsilon,
                    logOfMasterPMinusMomentumBeforeLeapfrog,
                    random
                );

                double acceptOtherTreePositionProbability = (double) otherHalfTree.acceptedLeapfrogCount / (tree.acceptedLeapfrogCount + otherHalfTree.acceptedLeapfrogCount);

                acceptOtherPositionWithProbability(
                    acceptOtherTreePositionProbability,
                    otherHalfTree,
                    random
                );

                tree.shouldContinueFlag = otherHalfTree.shouldContinueFlag && isNotUTurning();

                tree.acceptedLeapfrogCount += otherHalfTree.acceptedLeapfrogCount;
                tree.deltaLikelihoodOfLeapfrog += otherHalfTree.deltaLikelihoodOfLeapfrog;
                tree.treeSize += otherHalfTree.treeSize;
            }

            return tree;
        }

    }

    public TreeBuilder TreeBuilderBaseCase(List<Vertex<DoubleTensor>> latentVertices,
                                                   List<Vertex> probabilisticVertices,
                                                   LogProbGradientCalculator logProbGradientCalculator,
                                                   final List<? extends Vertex> sampleFromVertices,
                                                   Leapfrog leapfrog,
                                                   double u,
                                                   int buildDirection,
                                                   double epsilon,
                                                   double logOfMasterPMinusMomentumBeforeLeapfrog) {

        leapfrog = leapfrog.step(latentVertices, logProbGradientCalculator, epsilon * buildDirection);

        final double logOfMasterPAfterLeapfrog = ProbabilityCalculator.calculateLogProbFor(probabilisticVertices);

        final double logOfMasterPMinusMomentum = logOfMasterPAfterLeapfrog - leapfrog.halfDotProductMomentum();
        final int acceptedLeapfrogCount = u <= Math.exp(logOfMasterPMinusMomentum) ? 1 : 0;
        final boolean shouldContinueFlag = u < Math.exp(DELTA_MAX + logOfMasterPMinusMomentum);

        final Map<VertexId, ?> sampleAtAcceptedPosition = takeSample(sampleFromVertices);

        final double deltaLikelihoodOfLeapfrog = Math.min(
            1.0,
            Math.exp(logOfMasterPMinusMomentum - logOfMasterPMinusMomentumBeforeLeapfrog)
        );

        return new TreeBuilder(
            leapfrog,
            leapfrog,
            leapfrog.position,
            leapfrog.gradient,
            logOfMasterPAfterLeapfrog,
            sampleAtAcceptedPosition,
            acceptedLeapfrogCount,
            shouldContinueFlag,
            deltaLikelihoodOfLeapfrog,
            1
        );
    }

    public void acceptOtherPositionWithProbability(double probability,
                                                    TreeBuilder otherTree,
                                                    KeanuRandom random) {
        if (withProbability(probability, random)) {
            acceptedPosition = otherTree.acceptedPosition;
            gradientAtAcceptedPosition = otherTree.gradientAtAcceptedPosition;
            logOfMasterPAtAcceptedPosition = otherTree.logOfMasterPAtAcceptedPosition;
            sampleAtAcceptedPosition = otherTree.sampleAtAcceptedPosition;
        }
    }

    private boolean withProbability(double probability, KeanuRandom random) {
        return random.nextDouble() < probability;
    }

    public boolean isNotUTurning() {
        double forward = 0.0;
        double backward = 0.0;

        for (Map.Entry<VertexId, DoubleTensor> forwardPositionForLatent : leapForward.position.entrySet()) {

            final VertexId latentId = forwardPositionForLatent.getKey();
            final DoubleTensor forwardMinusBackward = forwardPositionForLatent.getValue().minus(
                leapBackward.position.get(latentId)
            );

            forward += forwardMinusBackward.times(leapForward.momentum.get(latentId)).sum();
            backward += forwardMinusBackward.timesInPlace(leapBackward.momentum.get(latentId)).sum();
        }

        return (forward >= 0.0) && (backward >= 0.0);
    }

    /**
     * This is meant to be used for tracking a sample while building tree.
     *
     * @param sampleFromVertices take samples from these vertices
     */
    private Map<VertexId, ?> takeSample(List<? extends Vertex> sampleFromVertices) {
        Map<VertexId, ?> sample = new HashMap<>();
        for (Vertex vertex : sampleFromVertices) {
            putValue(vertex, sample);
        }
        return sample;
    }

    private <T> void putValue(Vertex<T> vertex, Map<VertexId, ?> target) {
        ((Map<VertexId, T>) target).put(vertex.getId(), vertex.getValue());
    }

}