package io.improbable.keanu.algorithms.mcmc.proposal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static io.improbable.keanu.tensor.Tensor.SCALAR_SHAPE;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;

import io.improbable.keanu.algorithms.mcmc.adaptive.GaussianAdaptiveMcMcStrategy;
import io.improbable.keanu.distributions.continuous.Gaussian;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexId;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;

@RunWith(MockitoJUnitRunner.class)
public class GaussianProposalDistributionTest {

    public Proposal proposal;
    DoubleTensor currentState = DoubleTensor.create(4.2, 42.0).transpose();
    DoubleTensor proposedState = DoubleTensor.create(4.3, 43.0).transpose();

    @Mock
    public GaussianVertex vertex1;
    @Mock
    public GaussianVertex vertex2;
    private GaussianProposalDistribution proposalDistribution;
    private DoubleTensor sigma;

    @Before
    public void setUpProposalDistribution() throws Exception {
        sigma = DoubleTensor.scalar(1.);
        proposalDistribution = new GaussianProposalDistribution(sigma);
    }

    @Before
    public void setRandomSeed() throws Exception {
        KeanuRandom.setDefaultRandomSeed(0);
    }

    @Before
    public void setUpMocks() throws Exception {
        when(vertex1.getValue()).thenReturn(scalarValue(currentState, 0));
        when(vertex1.getShape()).thenReturn(SCALAR_SHAPE);
        when(vertex1.getId()).thenReturn(new VertexId());
        when(vertex2.getValue()).thenReturn(scalarValue(currentState, 1));
        when(vertex2.getShape()).thenReturn(SCALAR_SHAPE);
        when(vertex2.getId()).thenReturn(new VertexId());

        proposal = new Proposal();
        proposal.setProposal(vertex1, scalarValue(proposedState, 0));
        proposal.setProposal(vertex2, scalarValue(proposedState, 1));
    }

    private DoubleTensor scalarValue(DoubleTensor tensor, int index) {
        return DoubleTensor.scalar(tensor.getValue(index));
    }

    @Test
    public void theLogProbAtToIsGaussianAroundTheGivenPoint() {
        double logProb = proposalDistribution.logProbAtToGivenFrom(proposal);
        DoubleTensor expectedLogProb = Gaussian.withParameters(currentState, sigma).logProb(proposedState);
        assertThat(logProb, equalTo(expectedLogProb.sum()));
    }

    @Test
    public void theLogProbAtFromIsGaussianAroundTheGivenPoint() {
        double logProb = proposalDistribution.logProbAtFromGivenTo(proposal);
        DoubleTensor expectedLogProb = Gaussian.withParameters(proposedState, sigma).logProb(currentState);
        assertThat(logProb, equalTo(expectedLogProb.sum()));
    }

    @Test
    public void youCanUseAnAdaptiveStrategyForSigma() {
        GaussianAdaptiveMcMcStrategy strategy = mock(GaussianAdaptiveMcMcStrategy.class);
        when(strategy.getSigmaValue(any(VertexId.class))).thenReturn(DoubleTensor.ZERO_SCALAR);

        GaussianProposalDistribution proposalDistribution = new GaussianProposalDistribution(strategy);
        Set<Vertex> vertices = ImmutableSet.of(vertex1, vertex2);

        Proposal proposal = proposalDistribution.getProposal(vertices, KeanuRandom.getDefaultRandom());
        verify(strategy, times(vertices.size())).getSigmaValue(any(VertexId.class));

        proposal.apply();
        verify(strategy).onProposalAccepted(proposal);

        proposalDistribution.logProb(
            vertex1,
            scalarValue(currentState, 0),
            scalarValue(proposedState, 0)
        );
        verify(strategy, times(vertices.size() + 1)).getSigmaValue(any(VertexId.class));

        verifyNoMoreInteractions(strategy);
    }
}