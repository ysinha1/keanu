package io.improbable.keanu.algorithms.mcmc.adaptive;

import io.improbable.keanu.algorithms.mcmc.proposal.Proposal;

public interface AdaptiveMcMcStrategy {
    AdaptiveMcMcStrategy NONE = new AdaptiveMcMcStrategy() {
        public void onProposalAccepted(Proposal proposal) {
        }
    };

    void onProposalAccepted(Proposal proposal);
}
