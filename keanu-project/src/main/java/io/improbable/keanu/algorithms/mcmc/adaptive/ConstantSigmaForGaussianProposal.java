package io.improbable.keanu.algorithms.mcmc.adaptive;

import io.improbable.keanu.algorithms.mcmc.proposal.Proposal;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.VertexId;

public class ConstantSigmaForGaussianProposal implements GaussianAdaptiveMcMcStrategy {
    private final DoubleTensor sigma;

    public ConstantSigmaForGaussianProposal(DoubleTensor sigma) {
        this.sigma = sigma;
    }

    @Override
    public DoubleTensor getSigmaValue(VertexId id) {
        return sigma;
    }

    @Override
    public void onProposalAccepted(Proposal proposal) {
        // do nothing, because we return the same value of sigma no matter what the state history
    }
}
