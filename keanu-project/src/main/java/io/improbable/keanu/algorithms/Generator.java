package io.improbable.keanu.algorithms;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.improbable.keanu.tensor.generic.GenericTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.generic.probabilistic.discrete.CategoricalVertex;

public class Generator {

    private static final double CHANCE_OF_ADD = 1.0;
    private static final double CHANCE_OF_CHOOSING_EXISTING = 0.5;
    private static final double CHANCE_OF_PROBABILISTIC = 0.2;
    private static final double CHANCE_OF_OPERATION = 1 - CHANCE_OF_PROBABILISTIC;
    private static final String OP_PREFIX = "OP_";
    private static final String PROB_PREFIX = "PROB_";

    private List<DoubleVertex> vertices;
    private KeanuRandom random;
    private CategoricalVertex<Vertices, GenericTensor<Vertices>> categoricalVertex;
    private Map<Vertices, Class> vertexMap;

    public Generator() {
        this.vertices = initialiseGraph();
        this.random = new KeanuRandom();
        this.categoricalVertex = initialiseCategorical();
        this.vertexMap = initialiseVertexMap();
    }

    private List<DoubleVertex> initialiseGraph() {
        List<DoubleVertex> vertices = new ArrayList<>();
        DoubleVertex start = new ConstantDoubleVertex(5.0);
        vertices.add(start);
        return vertices;
    }

//    PROB_GAUSSIAN,
//    PROB_UNIFORM,
//    PROB_LOG_NORMAL,
//    PROB_GAMMA,
//    PROB_EXPONENTIAL,
//    PROB_CAUCHY,
//
//    OP_ADDITION,
//    OP_DIFFERENCE,
//    OP_DIVISION,
//    OP_MULTIPLICATION,
//    OP_POWER,
//
//    OP_EXP,
//    OP_SIGMOID,
//    OP_SIN

    private Map<Vertices, Class> initialiseVertexMap() {
        Map<Vertices, Class> map = new HashMap<>();
        map.put(Vertices.PROB_GAUSSIAN, GaussianVertex.class);
        return map;
    }

    private CategoricalVertex<Vertices, GenericTensor<Vertices>> initialiseCategorical() {
        LinkedHashMap<Vertices, DoubleVertex> selectableVertices = new LinkedHashMap<>();
        for (Vertices e : Vertices.values()) {
            String name = e.toString();
            if (name.startsWith(OP_PREFIX)) {
                selectableVertices.put(e, ConstantVertex.of(CHANCE_OF_OPERATION));
            } else if (name.startsWith(PROB_PREFIX)) {
                selectableVertices.put(e, ConstantVertex.of(CHANCE_OF_PROBABILISTIC));
            }
        }
        return new CategoricalVertex<>(selectableVertices);
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
        Vertices vertexToAdd = categoricalVertex.sample().scalar();
//        Class vertexClass = vertexMap.get(vertexToAdd);
        Class vertexClass = GaussianVertex.class;
        Constructor[] constructors = vertexClass.getConstructors();

        for (Constructor constructor : constructors) {
            boolean areAllDoubleVertex = true;
            Class[] parameterTypes = constructor.getParameterTypes();
            for (Class parameterClass : parameterTypes) {
                if (!parameterClass.isAssignableFrom(DoubleVertex.class)) {
                    areAllDoubleVertex = false;
                }
            }
            if (areAllDoubleVertex) {
                int parameterCount = constructor.getParameterCount();
                Object[] params = new Object[parameterCount];
                for (int i = 0; i < parameterCount; i++) {
                    double next = random.nextDouble();
                    if (next <= CHANCE_OF_CHOOSING_EXISTING) {
                        int randomIndex = random.nextInt(vertices.size());
                        DoubleVertex choice = vertices.get(randomIndex);
                        params[i] = choice;
                    } else {
                        DoubleVertex param = ConstantVertex.of(5.0);
                        params[i] = param;
                        vertices.add(param);
                    }
                }
                DoubleVertex newVertex = create(constructor, params);
                vertices.add(newVertex);
            }
        }
    }

    private void delete() {

    }

    private DoubleVertex create(Constructor constructor, Object[] params) {
        try {
            return (DoubleVertex) constructor.newInstance(params);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DoubleVertex> getVertices() {
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
