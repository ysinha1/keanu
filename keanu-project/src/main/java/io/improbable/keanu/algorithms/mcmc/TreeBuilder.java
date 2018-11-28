//package io.improbable.keanu.algorithms.mcmc;
//
//import java.util.List;
//import java.util.Map;
//
//import io.improbable.keanu.tensor.dbl.DoubleTensor;
//import io.improbable.keanu.vertices.ProbabilityCalculator;
//import io.improbable.keanu.vertices.Vertex;
//import io.improbable.keanu.vertices.VertexId;
//import io.improbable.keanu.vertices.dbl.KeanuRandom;
//import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.LogProbGradientCalculator;
//
//public class TreeBuilder {
//
//    Map<VertexId, DoubleTensor> positionBackward;
//    Map<VertexId, DoubleTensor> gradientBackward;
//    Map<VertexId, DoubleTensor> momentumBackward;
//    Map<VertexId, DoubleTensor> positionForward;
//    Map<VertexId, DoubleTensor> gradientForward;
//    Map<VertexId, DoubleTensor> momentumForward;
//    Map<VertexId, DoubleTensor> acceptedPosition;
//    Map<VertexId, DoubleTensor> gradientAtAcceptedPosition;
//    double logOfMasterPAtAcceptedPosition;
//    Map<VertexId, ?> sampleAtAcceptedPosition;
//    int acceptedLeapfrogCount;
//    boolean shouldContinueFlag;
//    double deltaLikelihoodOfLeapfrog;
//    double treeSize;
//
//    TreeBuilder(Map<VertexId, DoubleTensor> positionBackward,
//              Map<VertexId, DoubleTensor> gradientBackward,
//              Map<VertexId, DoubleTensor> momentumBackward,
//              Map<VertexId, DoubleTensor> positionForward,
//              Map<VertexId, DoubleTensor> gradientForward,
//              Map<VertexId, DoubleTensor> momentumForward,
//              Map<VertexId, DoubleTensor> acceptedPosition,
//              Map<VertexId, DoubleTensor> gradientAtAcceptedPosition,
//              double logProbAtAcceptedPosition,
//              Map<VertexId, ?> sampleAtAcceptedPosition,
//              int acceptedLeapfrogCount,
//              boolean shouldContinueFlag,
//              double deltaLikelihoodOfLeapfrog,
//              double treeSize) {
//
//        this.positionBackward = positionBackward;
//        this.gradientBackward = gradientBackward;
//        this.momentumBackward = momentumBackward;
//        this.positionForward = positionForward;
//        this.gradientForward = gradientForward;
//        this.momentumForward = momentumForward;
//        this.acceptedPosition = acceptedPosition;
//        this.gradientAtAcceptedPosition = gradientAtAcceptedPosition;
//        this.logOfMasterPAtAcceptedPosition = logProbAtAcceptedPosition;
//        this.sampleAtAcceptedPosition = sampleAtAcceptedPosition;
//        this.acceptedLeapfrogCount = acceptedLeapfrogCount;
//        this.shouldContinueFlag = shouldContinueFlag;
//        this.deltaLikelihoodOfLeapfrog = deltaLikelihoodOfLeapfrog;
//        this.treeSize = treeSize;
//    }
//
//    private static TreeBuilder buildOtherHalfOfTree(TreeBuilder currentTree,
//                                                  List<Vertex<DoubleTensor>> latentVertices,
//                                                  List<Vertex> probabilisticVertices,
//                                                  LogProbGradientCalculator logProbGradientCalculator,
//                                                  final List<? extends Vertex> sampleFromVertices,
//                                                  double u,
//                                                  int buildDirection,
//                                                  int treeHeight,
//                                                  double epsilon,
//                                                  double logOfMasterPMinusMomentumBeforeLeapfrog,
//                                                  KeanuRandom random) {
//
//        TreeBuilder otherHalfTree;
//
//        if (buildDirection == -1) {
//
//            otherHalfTree = buildTree(
//                latentVertices,
//                probabilisticVertices,
//                logProbGradientCalculator,
//                sampleFromVertices,
//                currentTree.positionBackward,
//                currentTree.gradientBackward,
//                currentTree.momentumBackward,
//                u,
//                buildDirection,
//                treeHeight,
//                epsilon,
//                logOfMasterPMinusMomentumBeforeLeapfrog,
//                random
//            );
//
//            currentTree.positionBackward = otherHalfTree.positionBackward;
//            currentTree.momentumBackward = otherHalfTree.momentumBackward;
//            currentTree.gradientBackward = otherHalfTree.gradientBackward;
//
//        } else {
//
//            otherHalfTree = buildTree(
//                latentVertices,
//                probabilisticVertices,
//                logProbGradientCalculator,
//                sampleFromVertices,
//                currentTree.positionForward,
//                currentTree.gradientForward,
//                currentTree.momentumForward,
//                u,
//                buildDirection,
//                treeHeight,
//                epsilon,
//                logOfMasterPMinusMomentumBeforeLeapfrog,
//                random
//            );
//
//            currentTree.positionForward = otherHalfTree.positionForward;
//            currentTree.momentumForward = otherHalfTree.momentumForward;
//            currentTree.gradientForward = otherHalfTree.gradientForward;
//        }
//
//        return otherHalfTree;
//    }
//
//    private static TreeBuilder buildTree(List<Vertex<DoubleTensor>> latentVertices,
//                                       List<Vertex> probabilisticVertices,
//                                       LogProbGradientCalculator logProbGradientCalculator,
//                                       final List<? extends Vertex> sampleFromVertices,
//                                       Map<VertexId, DoubleTensor> position,
//                                       Map<VertexId, DoubleTensor> gradient,
//                                       Map<VertexId, DoubleTensor> momentum,
//                                       double u,
//                                       int buildDirection,
//                                       int treeHeight,
//                                       double epsilon,
//                                       double logOfMasterPMinusMomentumBeforeLeapfrog,
//                                       KeanuRandom random) {
//        if (treeHeight == 0) {
//
//            //Base case-take one leapfrog step in the build direction
//            return TreeBuilderBaseCase(latentVertices,
//                probabilisticVertices,
//                logProbGradientCalculator,
//                sampleFromVertices,
//                position,
//                gradient,
//                momentum,
//                u,
//                buildDirection,
//                epsilon,
//                logOfMasterPMinusMomentumBeforeLeapfrog
//            );
//
//        } else {
//            //Recursion-implicitly build the left and right subtrees.
//
//            TreeBuilder tree = buildTree(
//                latentVertices,
//                probabilisticVertices,
//                logProbGradientCalculator,
//                sampleFromVertices,
//                position,
//                gradient,
//                momentum,
//                u,
//                buildDirection,
//                treeHeight - 1,
//                epsilon,
//                logOfMasterPMinusMomentumBeforeLeapfrog,
//                random
//            );
//
//            //Should continue building other half if first half's shouldContinueFlag is true
//            if (tree.shouldContinueFlag) {
//
//                TreeBuilder otherHalfTree = buildOtherHalfOfTree(
//                    tree,
//                    latentVertices,
//                    probabilisticVertices,
//                    logProbGradientCalculator,
//                    sampleFromVertices,
//                    u,
//                    buildDirection,
//                    treeHeight - 1,
//                    epsilon,
//                    logOfMasterPMinusMomentumBeforeLeapfrog,
//                    random
//                );
//
//                double acceptOtherTreePositionProbability = (double) otherHalfTree.acceptedLeapfrogCount / (tree.acceptedLeapfrogCount + otherHalfTree.acceptedLeapfrogCount);
//
//                if (tree.acceptedLeapfrogCount == 0 && otherHalfTree.acceptedLeapfrogCount == 0) {
//                    System.out.println("nan");
//                }
//                acceptOtherPositionWithProbability(
//                    acceptOtherTreePositionProbability,
//                    tree, otherHalfTree,
//                    random
//                );
//
//                tree.shouldContinueFlag = otherHalfTree.shouldContinueFlag && isNotUTurning(
//                    tree.positionForward,
//                    tree.positionBackward,
//                    tree.momentumForward,
//                    tree.momentumBackward
//                );
//
//                tree.acceptedLeapfrogCount += otherHalfTree.acceptedLeapfrogCount;
//                System.out.println("Accepted count: " + tree.acceptedLeapfrogCount);
//                tree.deltaLikelihoodOfLeapfrog += otherHalfTree.deltaLikelihoodOfLeapfrog;
//                tree.treeSize += otherHalfTree.treeSize;
//            }
//
//            return tree;
//        }
//
//    }
//
//    private static TreeBuilder TreeBuilderBaseCase(List<Vertex<DoubleTensor>> latentVertices,
//                                               List<Vertex> probabilisticVertices,
//                                               LogProbGradientCalculator logProbGradientCalculator,
//                                               final List<? extends Vertex> sampleFromVertices,
//                                               Map<VertexId, DoubleTensor> position,
//                                               Map<VertexId, DoubleTensor> gradient,
//                                               Map<VertexId, DoubleTensor> momentum,
//                                               double u,
//                                               int buildDirection,
//                                               double epsilon,
//                                               double logOfMasterPMinusMomentumBeforeLeapfrog) {
//
//        NUTSSampler.LeapFrogged leapfrog = leapfrog(
//            latentVertices,
//            logProbGradientCalculator,
//            position,
//            gradient,
//            momentum,
//            epsilon * buildDirection
//        );
//
//        final double logOfMasterPAfterLeapfrog = ProbabilityCalculator.calculateLogProbFor(probabilisticVertices);
//
//        final double logOfMasterPMinusMomentum = logOfMasterPAfterLeapfrog - 0.5 * dotProduct(leapfrog.momentum);
//        final int acceptedLeapfrogCount = u <= Math.exp(logOfMasterPMinusMomentum) ? 1 : 0;
//        final boolean shouldContinueFlag = u < Math.exp(DELTA_MAX + logOfMasterPMinusMomentum);
//
//        final Map<VertexId, ?> sampleAtAcceptedPosition = takeSample(sampleFromVertices);
//
//        final double deltaLikelihoodOfLeapfrog = Math.min(
//            1.0,
//            Math.exp(logOfMasterPMinusMomentum - logOfMasterPMinusMomentumBeforeLeapfrog)
//        );
//
//        return new TreeBuilder(
//            leapfrog.position,
//            leapfrog.gradient,
//            leapfrog.momentum,
//            leapfrog.position,
//            leapfrog.gradient,
//            leapfrog.momentum,
//            leapfrog.position,
//            leapfrog.gradient,
//            logOfMasterPAfterLeapfrog,
//            sampleAtAcceptedPosition,
//            acceptedLeapfrogCount,
//            shouldContinueFlag,
//            deltaLikelihoodOfLeapfrog,
//            1
//        );
//    }
//
//    public static void acceptOtherPositionWithProbability(double probability,
//                                                          TreeBuilder tree,
//                                                          TreeBuilder otherTree,
//                                                          KeanuRandom random) {
//        if (withProbability(probability, random)) {
//            tree.acceptedPosition = otherTree.acceptedPosition;
//            tree.gradientAtAcceptedPosition = otherTree.gradientAtAcceptedPosition;
//            tree.logOfMasterPAtAcceptedPosition = otherTree.logOfMasterPAtAcceptedPosition;
//            tree.sampleAtAcceptedPosition = otherTree.sampleAtAcceptedPosition;
//        }
//    }
//
//    private static boolean withProbability(double probability, KeanuRandom random) {
//        return random.nextDouble() < probability;
//    }
//
//}
