package io.improbable.keanu.algorithms.mcmc.adaptive;

import io.improbable.keanu.algorithms.mcmc.proposal.Proposal;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.VertexId;

public class AcceptanceRateTrackingAdaptiveMcMcStrategy implements GaussianAdaptiveMcMcStrategy {
    private long numAccepted;
    private long numRejected;

    @Override
    public DoubleTensor getSigmaValue(VertexId id) {
        // this is a toy example - you wouldn't really set sigma to be so directly related to the acceptance rate
        double acceptanceRate = ((double) numAccepted) / (numAccepted + numRejected);
        return DoubleTensor.scalar(acceptanceRate);
    }

    @Override
    public void onProposalAccepted(Proposal proposal) {
        numAccepted++;
    }

    @Override
    public void onProposalRejected(Proposal proposal) {
        numRejected++;
    }
}
