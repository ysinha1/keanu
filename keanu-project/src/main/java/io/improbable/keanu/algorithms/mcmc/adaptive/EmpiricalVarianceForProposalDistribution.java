package io.improbable.keanu.algorithms.mcmc.adaptive;

import java.util.Map;

import com.google.common.collect.Maps;

import io.improbable.keanu.algorithms.mcmc.proposal.Proposal;
import io.improbable.keanu.algorithms.statistics.SummaryStatistics;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexId;
import io.improbable.keanu.vertices.dbl.DoubleVertex;

public class EmpiricalVarianceForProposalDistribution implements GaussianAdaptiveMcMcStrategy {

    Map<VertexId, SummaryStatistics> statisticsForEachVertex = Maps.newHashMap();

    @Override
    public DoubleTensor getSigmaValue(VertexId id) {
        return statisticsForEachVertex.get(id).getStandardDeviation();
    }

    @Override
    public void onProposalAccepted(Proposal proposal) {

        for (Vertex vertex : proposal.getVerticesWithProposal()) {
            if (vertex instanceof DoubleVertex) {
                statisticsForEachVertex.putIfAbsent(vertex.getId(), new SummaryStatistics());
                SummaryStatistics statistics = statisticsForEachVertex.get(vertex.getId());
                DoubleTensor value = proposal.getProposalTo((DoubleVertex) vertex);
                statistics.addValue(value);
            }
        }
    }
}
