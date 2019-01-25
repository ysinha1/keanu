package io.improbable.keanu.algorithms.mcmc;

import io.improbable.keanu.algorithms.mcmc.proposal.Proposal;
import io.improbable.keanu.algorithms.variational.optimizer.KeanuProbabilisticModel;
import io.improbable.keanu.algorithms.variational.optimizer.LambdaSectionSnapshot;
import io.improbable.keanu.network.NetworkSnapshot;
import io.improbable.keanu.vertices.Vertex;

import java.util.List;
import java.util.Set;

public class RollBackToCachedValuesOnRejection implements ProposalRejectionStrategy {
    private final LambdaSectionSnapshot lambdaSectionSnapshot;
    private final KeanuProbabilisticModel model;
    private NetworkSnapshot networkSnapshot;

    public RollBackToCachedValuesOnRejection(KeanuProbabilisticModel model) {
        lambdaSectionSnapshot = new LambdaSectionSnapshot(model.getLatentVertices());
        this.model = model;
    }

    @Override
    public void onProposalCreated(Proposal proposal) {
        Set<Vertex> affectedVariables = lambdaSectionSnapshot.getAllVerticesAffectedBy(proposal.getVariablesWithProposal());
        networkSnapshot = NetworkSnapshot.create(affectedVariables);
    }

    @Override
    public void onProposalRejected(Proposal proposal) {
        networkSnapshot.apply();
        model.setNeedToRecalculateLogProb();
    }
}
