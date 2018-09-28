package io.improbable.keanu.algorithms.mcmc.adaptive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import static io.improbable.keanu.tensor.TensorMatchers.hasValue;

import org.junit.Test;

import io.improbable.keanu.algorithms.mcmc.proposal.Proposal;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexId;
import io.improbable.keanu.vertices.mock.MockVertexProvider;

public class EmpiricalVarianceForProposalDistributionTest {

    GaussianAdaptiveMcMcStrategy adaptiveStrategy = new EmpiricalVarianceForProposalDistribution();

    MockVertexProvider mockVertex = new MockVertexProvider();
    private Vertex vertex1 = mockVertex.containing(1.);
    private Vertex vertex2 = mockVertex.containing(2.);

    @Test
    public void itCalculatesTheEmpiricalVarianceOfTheAcceptedStates() {
        Proposal proposal = new Proposal(adaptiveStrategy);
        proposal.setProposal(vertex1, DoubleTensor.scalar(1.1));
        proposal.setProposal(vertex2, DoubleTensor.scalar(2.2));
        adaptiveStrategy.onProposalAccepted(proposal);

        proposal.setProposal(vertex1, DoubleTensor.scalar(1.3));
        proposal.setProposal(vertex2, DoubleTensor.scalar(2.6));
        adaptiveStrategy.onProposalAccepted(proposal);

        assertThat(adaptiveStrategy.getSigmaValue(vertex1.getId()), hasValue(closeTo(Math.sqrt(0.02), 1e-6)));
        assertThat(adaptiveStrategy.getSigmaValue(vertex2.getId()), hasValue(closeTo(Math.sqrt(0.08), 1e-6)));
    }

    @Test(expected = NullPointerException.class)
    public void itThrowsIfYouRequestTheSigmaOfAVertexItHasntSeenBefore() {
        adaptiveStrategy.getSigmaValue(new VertexId());
    }
}