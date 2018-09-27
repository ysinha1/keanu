package io.improbable.keanu.algorithms.mcmc.proposal;

import java.util.Set;

import io.improbable.keanu.algorithms.mcmc.adaptive.ConstantSigmaForGaussianProposal;
import io.improbable.keanu.algorithms.mcmc.adaptive.GaussianAdaptiveMcMcStrategy;
import io.improbable.keanu.distributions.ContinuousDistribution;
import io.improbable.keanu.distributions.continuous.Gaussian;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Probabilistic;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;

public class GaussianProposalDistribution implements ProposalDistribution {

    private final GaussianAdaptiveMcMcStrategy adaptiveStrategy;

    public GaussianProposalDistribution(DoubleTensor sigma) {
        this(new ConstantSigmaForGaussianProposal(sigma));
    }

    public GaussianProposalDistribution(GaussianAdaptiveMcMcStrategy adaptiveStrategy) {
        this.adaptiveStrategy = adaptiveStrategy;
    }

    @Override
    public Proposal getProposal(Set<Vertex> vertices, KeanuRandom random) {
        Proposal proposal = new Proposal(adaptiveStrategy);
        for (Vertex vertex : vertices) {
            DoubleTensor sigma = adaptiveStrategy.getSigmaValue();
            ContinuousDistribution proposalDistribution = Gaussian.withParameters((DoubleTensor) vertex.getValue(), sigma);
            proposal.setProposal((DoubleVertex) vertex, proposalDistribution.sample(vertex.getShape(), random));
        }
        return proposal;
    }

    @Override
    public <T> double logProb(Probabilistic<T> vertex, T ofValue, T givenValue) {
        if (!(ofValue instanceof DoubleTensor)) {
            throw new ClassCastException("Only DoubleTensor values are supported - not " + ofValue.getClass().getSimpleName());
        }

        DoubleTensor sigma = adaptiveStrategy.getSigmaValue();
        ContinuousDistribution proposalDistribution = Gaussian.withParameters((DoubleTensor) ofValue, sigma);
        return proposalDistribution.logProb((DoubleTensor) givenValue).sum();
    }

}
