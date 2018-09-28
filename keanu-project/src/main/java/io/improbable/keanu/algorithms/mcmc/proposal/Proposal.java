package io.improbable.keanu.algorithms.mcmc.proposal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.improbable.keanu.algorithms.mcmc.adaptive.AdaptiveMcMcStrategy;
import io.improbable.keanu.vertices.Vertex;

public class Proposal {

    private final Map<Vertex, Object> perVertexProposalTo = new HashMap<>();
    private final Map<Vertex, Object> perVertexProposalFrom = new HashMap<>();
    private final AdaptiveMcMcStrategy adaptiveStrategy;

    public Proposal() {
        this(AdaptiveMcMcStrategy.NONE);
    }

    public Proposal(AdaptiveMcMcStrategy adaptiveStrategy) {
        this.adaptiveStrategy = adaptiveStrategy;
    }

    public <T> void setProposal(Vertex<T> vertex, T to) {
        perVertexProposalFrom.put(vertex, vertex.getValue());
        perVertexProposalTo.put(vertex, to);
    }

    public <T> T getProposalTo(Vertex<T> vertex) {
        return (T) perVertexProposalTo.get(vertex);
    }

    public <T> T getProposalFrom(Vertex<T> vertex) {
        return (T) perVertexProposalFrom.get(vertex);
    }

    public Set<Vertex> getVerticesWithProposal() {
        return perVertexProposalTo.keySet();
    }

    public void apply() {
        Set<Vertex> vertices = perVertexProposalTo.keySet();
        for (Vertex v : vertices) {
            v.setValue(getProposalTo(v));
        }
        adaptiveStrategy.onProposalAccepted(this);
    }

    public void reject() {
        Set<Vertex> vertices = perVertexProposalTo.keySet();
        for (Vertex v : vertices) {
            v.setValue(getProposalFrom(v));
        }
        adaptiveStrategy.onProposalRejected(this);
    }

}
