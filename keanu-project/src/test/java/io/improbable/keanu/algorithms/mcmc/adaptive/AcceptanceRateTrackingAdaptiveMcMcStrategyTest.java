package io.improbable.keanu.algorithms.mcmc.adaptive;

import static org.hamcrest.MatcherAssert.assertThat;

import static io.improbable.keanu.tensor.TensorMatchers.hasValue;

import org.junit.Test;

import io.improbable.keanu.algorithms.mcmc.proposal.Proposal;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.mock.MockVertexProvider;

public class AcceptanceRateTrackingAdaptiveMcMcStrategyTest {

    GaussianAdaptiveMcMcStrategy  adaptiveStrategy = new AcceptanceRateTrackingAdaptiveMcMcStrategy();

    MockVertexProvider mockVertex = new MockVertexProvider();
    private Vertex vertex1 = mockVertex.containing(1.);
    private Vertex vertex2 = mockVertex.containing(2.);

    @Test
    public void itUsesTheAcceptanceRatesOfTheProposals() {
        Proposal proposal = new Proposal(adaptiveStrategy);
        proposal.setProposal(vertex1, DoubleTensor.scalar(1.1));
        proposal.setProposal(vertex2, DoubleTensor.scalar(2.2));
        adaptiveStrategy.onProposalAccepted(proposal);
        adaptiveStrategy.onProposalRejected(proposal);
        adaptiveStrategy.onProposalRejected(proposal);
        adaptiveStrategy.onProposalRejected(proposal);

        assertThat(adaptiveStrategy.getSigmaValue(vertex1.getId()), hasValue(0.25));
        assertThat(adaptiveStrategy.getSigmaValue(vertex2.getId()), hasValue(0.25));
    }
}
