package io.improbable.keanu.algorithms.mcmc.NUTS;

import io.improbable.keanu.algorithms.mcmc.SamplingAlgorithm;
import io.improbable.keanu.network.NetworkState;
import io.improbable.keanu.network.SimpleNetworkState;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.ProbabilityCalculator;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexId;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.LogProbGradientCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Algorithm 6: "No-U-Turn Sampler with Dual Averaging".
 * The No-U-Turn Sampler: Adaptively Setting Path Lengths in Hamiltonian Monte Carlo
 * https://arxiv.org/pdf/1111.4246.pdf
 */
public class NUTSSampler implements SamplingAlgorithm {

    public Double stepSize;

    private static final double STABILISER = 10;
    private static final double SHRINKAGE_FACTOR = 0.05;
    private static final double TEND_TO_ZERO_EXPONENT = 0.75;

    private final KeanuRandom random;
    private final List<Vertex<DoubleTensor>> latentVertices;
    private final List<? extends Vertex> sampleFromVertices;
    private final List<Vertex> probabilisticVertices;
    private final int maxTreeHeight;
    private final boolean adaptEnabled;
    private final AutoTune autoTune;
    private final TreeBuilder tree;
    private final LogProbGradientCalculator logProbGradientCalculator;

    private int sampleNum;

    /**
     * @param sampleFromVertices        vertices to sample from
     * @param latentVertices            vertices that represent latent variables
     * @param probabilisticVertices     vertices that contribute to total log probability (i.e. latent + observed)
     * @param logProbGradientCalculator gradient calculator for diff of log prob with respect to latents
     * @param adaptEnabled              enable the NUTS step size adaptation
     * @param autoTune                  configuration for tuning the stepsize, if adaptEnabled
     * @param tree                      initial tree that will contain the state of the tree build
     * @param stepSize                  The initial step size. A heuristic will be used to determine a suitable initial stepsize if none
     *                                  is given.
     * @param maxTreeHeight             The largest tree height before stopping the hamilitonian process
     * @param random                    the source of randomness
     */
    public NUTSSampler(List<? extends Vertex> sampleFromVertices,
                       List<Vertex<DoubleTensor>> latentVertices,
                       List<Vertex> probabilisticVertices,
                       LogProbGradientCalculator logProbGradientCalculator,
                       boolean adaptEnabled,
                       AutoTune autoTune,
                       TreeBuilder tree,
                       Double stepSize,
                       int maxTreeHeight,
                       KeanuRandom random) {

        this.sampleFromVertices = sampleFromVertices;
        this.probabilisticVertices = probabilisticVertices;
        this.latentVertices = latentVertices;
        this.logProbGradientCalculator = logProbGradientCalculator;

        this.sampleNum = 1;
        this.stepSize = stepSize;
        this.tree = tree;
        this.autoTune = autoTune;
        this.maxTreeHeight = maxTreeHeight;
        this.adaptEnabled = adaptEnabled;

        this.random = random;
    }

    @Override
    public void sample(Map<VertexId, List<?>> samples, List<Double> logOfMasterPForEachSample) {
        step();
        addSampleFromCache(samples, tree.sampleAtAcceptedPosition);
        logOfMasterPForEachSample.add(tree.logOfMasterPAtAcceptedPosition);
    }

    @Override
    public NetworkState sample() {
        step();
        return new SimpleNetworkState(tree.sampleAtAcceptedPosition);
    }

    @Override
    public void step() {

        initializeMomentumForEachVertex(latentVertices, tree.leapForward.momentum, random);
        cache(tree.leapForward.momentum, tree.leapBackward.momentum);

        double logOfMasterPMinusMomentumBeforeLeapfrog = tree.logOfMasterPAtAcceptedPosition - tree.leapForward.halfDotProductMomentum();
        double logU = Math.log(random.nextDouble()) + logOfMasterPMinusMomentumBeforeLeapfrog;

        int treeHeight = 0;
        tree.shouldContinueFlag = true;
        tree.acceptedLeapfrogCount = 1;

        while (tree.shouldContinueFlag && treeHeight < maxTreeHeight) {

            //build tree direction -1 = backwards OR 1 = forwards
            int buildDirection = random.nextBoolean() ? 1 : -1;

            TreeBuilder otherHalfTree = tree.buildOtherHalfOfTree(
                tree,
                latentVertices,
                probabilisticVertices,
                logProbGradientCalculator,
                sampleFromVertices,
                logU,
                buildDirection,
                treeHeight,
                stepSize,
                logOfMasterPMinusMomentumBeforeLeapfrog,
                random
            );

            if (otherHalfTree.shouldContinueFlag) {
                final double acceptanceProb = (double) otherHalfTree.acceptedLeapfrogCount / tree.acceptedLeapfrogCount;

               tree.acceptOtherPositionWithProbability(
                    acceptanceProb,
                    otherHalfTree,
                    random
                );
            }

            tree.acceptedLeapfrogCount += otherHalfTree.acceptedLeapfrogCount;

            tree.deltaLikelihoodOfLeapfrog = otherHalfTree.deltaLikelihoodOfLeapfrog;
            tree.treeSize = otherHalfTree.treeSize;

            tree.shouldContinueFlag = otherHalfTree.shouldContinueFlag && tree.isNotUTurning();

            treeHeight++;
        }

        if (this.adaptEnabled) {
            stepSize = adaptStepSize(autoTune, tree, sampleNum);
        }

        tree.leapForward.position = tree.acceptedPosition;
        tree.leapForward.gradient = tree.gradientAtAcceptedPosition;
        tree.leapBackward.position = tree.acceptedPosition;
        tree.leapBackward.gradient = tree.gradientAtAcceptedPosition;

        sampleNum++;
    }

    private static void initializeMomentumForEachVertex(List<Vertex<DoubleTensor>> vertices,
                                                        Map<VertexId, DoubleTensor> momentums,
                                                        KeanuRandom random) {
        for (Vertex<DoubleTensor> vertex : vertices) {
            momentums.put(vertex.getId(), random.nextGaussian(vertex.getShape()));
        }

    }

    private static void cache(Map<VertexId, DoubleTensor> from, Map<VertexId, DoubleTensor> to) {
        for (Map.Entry<VertexId, DoubleTensor> entry : from.entrySet()) {
            to.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * This is used to save of the sample from the uniformly chosen acceptedPosition position
     *
     * @param samples      samples taken already
     * @param cachedSample a cached sample from before leapfrog
     */
    private static void addSampleFromCache(Map<VertexId, List<?>> samples, Map<VertexId, ?> cachedSample) {
        for (Map.Entry<VertexId, ?> sampleEntry : cachedSample.entrySet()) {
            addSampleForVertex(sampleEntry.getKey(), sampleEntry.getValue(), samples);
        }
    }

    private static <T> void addSampleForVertex(VertexId id, T value, Map<VertexId, List<?>> samples) {
        List<T> samplesForVertex = (List<T>) samples.computeIfAbsent(id, v -> new ArrayList<T>());
        samplesForVertex.add(value);
    }

    /**
     * Taken from algorithm 4 in https://arxiv.org/pdf/1111.4246.pdf.
     */
    static double findStartingStepSize(Map<VertexId, DoubleTensor> position,
                                       Map<VertexId, DoubleTensor> gradient,
                                       List<Vertex<DoubleTensor>> vertices,
                                       List<Vertex> probabilisticVertices,
                                       LogProbGradientCalculator logProbGradientCalculator,
                                       double initialLogOfMasterP,
                                       KeanuRandom random) {
        double stepsize = 1;
        Map<VertexId, DoubleTensor> momentums = new HashMap<>();
        initializeMomentumForEachVertex(vertices, momentums, random);

        Leapfrog leapfrog = new Leapfrog(position, momentums, gradient);
        double pThetaR = initialLogOfMasterP - leapfrog.halfDotProductMomentum();

        Leapfrog delta = leapfrog.step(vertices, logProbGradientCalculator, stepsize);

        double probAfterLeapfrog = ProbabilityCalculator.calculateLogProbFor(probabilisticVertices);
        double pThetaRAfterLeapFrog = probAfterLeapfrog - delta.halfDotProductMomentum();

        double logLikelihoodRatio = pThetaRAfterLeapFrog - pThetaR;
        double scalingFactor = logLikelihoodRatio > Math.log(0.5) ? 1 : -1;

        while (scalingFactor * logLikelihoodRatio > -scalingFactor * Math.log(2)) {
            stepsize = stepsize * Math.pow(2, scalingFactor);

            delta = leapfrog.step(vertices, logProbGradientCalculator, stepsize);
            probAfterLeapfrog = ProbabilityCalculator.calculateLogProbFor(probabilisticVertices);
            pThetaRAfterLeapFrog = probAfterLeapfrog - delta.halfDotProductMomentum();

            logLikelihoodRatio = pThetaRAfterLeapFrog - pThetaR;
        }

        return stepsize;
    }

    /**
     * Taken from algorithm 5 in https://arxiv.org/pdf/1111.4246.pdf.
     */
    static double adaptStepSize(AutoTune autoTune, TreeBuilder tree, int sampleNum) {

        if (sampleNum <= autoTune.adaptCount) {

            //1/(m+t0)
            double percentageLeftToTune = (1 / (sampleNum + STABILISER));

            //(1 - 1/(m+t0)) * Hm-1
            double proportionalAcceptanceProb = (1 - percentageLeftToTune) * autoTune.averageAcceptanceProb;

            //alpha/nu_alpha
            double averageDeltaLikelihoodLeapfrog = tree.deltaLikelihoodOfLeapfrog / tree.treeSize;

            //delta - alpha/nu_alpha
            double acceptanceProb = autoTune.targetAcceptanceProb - averageDeltaLikelihoodLeapfrog;

            //Hm = (1-1/(m+t0)) * Hm-1 + (1/(m+t0)) * (delta - (alpha/nu_alpha))
            double updatedAverageAcceptanceProb = proportionalAcceptanceProb + (percentageLeftToTune * acceptanceProb);

            //sqrt(mu)/gamma
            double shrunkSampleCount = Math.sqrt(sampleNum) / SHRINKAGE_FACTOR;

            //log(epsilon_m) = mu - (sqrt(m)/gamma) * Hm
            double updatedLogStepSize = autoTune.shrinkageTarget - (shrunkSampleCount * updatedAverageAcceptanceProb);

            //m^-k
            double tendToZero = Math.pow(sampleNum, -TEND_TO_ZERO_EXPONENT);

            //m^-k * log(epsilon_m)
            double reducedStepSize = tendToZero * updatedLogStepSize;

            //(1-m^-k) * log(epsilon_bar_m-1)
            double increasedStepSizeFrozen = (1 - tendToZero) * autoTune.logStepSizeFrozen;

            //log(epsilon_bar_m) = m^-k * log(epsilon_m) + (1 - m^-k) * log(epsilon_bar_m-1)
            autoTune.logStepSizeFrozen = reducedStepSize + increasedStepSizeFrozen;

            autoTune.averageAcceptanceProb = updatedAverageAcceptanceProb;
            autoTune.logStepSize = updatedLogStepSize;
            return Math.exp(autoTune.logStepSize);
        } else {

            return Math.exp(autoTune.logStepSizeFrozen);
        }
    }

    static class AutoTune {

        double stepSize;
        double averageAcceptanceProb;
        double targetAcceptanceProb;
        double logStepSize;
        double logStepSizeFrozen;
        double adaptCount;
        double shrinkageTarget;

        AutoTune(double stepSize, double targetAcceptanceProb, int adaptCount) {
            this.stepSize = stepSize;
            this.averageAcceptanceProb = 0;
            this.targetAcceptanceProb = targetAcceptanceProb;
            this.logStepSize = Math.log(logStepSize);
            this.logStepSizeFrozen = Math.log(1);
            this.adaptCount = adaptCount;
            this.shrinkageTarget = Math.log(10 * stepSize);
        }
    }
}
