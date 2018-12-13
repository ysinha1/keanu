package io.improbable.keanu.algorithms;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.generic.probabilistic.discrete.CategoricalVertex;

public class Generator {

    private static final double CHANCE_OF_ADD = 1.0;
    private static final double CHANCE_OF_PROBABILISTIC = 0.2;
    private static final double CHANCE_OF_OPERATION = 1 - CHANCE_OF_PROBABILISTIC;
    private static final String OP_PREFIX = "OP_";
    private static final String PROB_PREFIX = "PROB_";

    private Set<DoubleVertex> vertices;
    private KeanuRandom random;
    private CategoricalVertex categoricalVertex;

    public Generator() {
        this.vertices = initialiseGraph();
        this.random = new KeanuRandom();
        this.categoricalVertex = initialiseCategorical();
    }

    private Set<DoubleVertex> initialiseGraph() {
        Set<DoubleVertex> vertices = new HashSet<>();
        DoubleVertex start = new ConstantDoubleVertex(5.0);
        vertices.add(start);
        return vertices;
    }

    private CategoricalVertex initialiseCategorical() {
        LinkedHashMap<Vertices, DoubleVertex> selectableVertices = new LinkedHashMap<>();
        for (Vertices e : Vertices.values()) {
            String name = e.toString();
            if (name.startsWith(OP_PREFIX)) {
                selectableVertices.put(e, ConstantVertex.of(CHANCE_OF_OPERATION));
            } else if (name.startsWith(PROB_PREFIX)) {
                selectableVertices.put(e, ConstantVertex.of(CHANCE_OF_PROBABILISTIC));
            }
        }
        return new CategoricalVertex(selectableVertices);
    }

    public void transition() {
        double next = random.nextDouble();
        if (next <= CHANCE_OF_ADD) {
            add();
        } else {
            delete();
        }
    }

    private void add() {

    }

    private void delete() {

    }

    public Set<DoubleVertex> getVertices() {
        return vertices;
    }

    public CategoricalVertex getCategoricalVertex() {
        return categoricalVertex;
    }

    enum Vertices {

        PROB_GAUSSIAN,
        PROB_UNIFORM,
        PROB_LOG_NORMAL,
        PROB_GAMMA,
        PROB_EXPONENTIAL,
        PROB_CAUCHY,

        OP_ADDITION,
        OP_DIFFERENCE,
        OP_DIVISION,
        OP_MULTIPLICATION,
        OP_POWER,

        OP_EXP,
        OP_SIGMOID,
        OP_SIN

    }
}
