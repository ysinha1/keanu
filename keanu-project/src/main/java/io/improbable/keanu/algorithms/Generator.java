package io.improbable.keanu.algorithms;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.improbable.keanu.tensor.generic.GenericTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.AdditionVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.DifferenceVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.DivisionVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.MultiplicationVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.PowerVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.ExpVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.SigmoidVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.SinVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.CauchyVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.ExponentialVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GammaVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.LogNormalVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.generic.probabilistic.discrete.CategoricalVertex;

public class Generator {

    private static final double CHANCE_OF_ADD = 1.0;
    private static final double CHANCE_OF_CHOOSING_EXISTING = 0.75;
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

    private Map<Vertices, Class> initialiseVertexMap() {
        Map<Vertices, Class> map = new HashMap<>();
        map.put(Vertices.PROB_GAUSSIAN, GaussianVertex.class);
        map.put(Vertices.PROB_UNIFORM, UniformVertex.class);
        map.put(Vertices.PROB_LOG_NORMAL, LogNormalVertex.class);
        map.put(Vertices.PROB_GAMMA, GammaVertex.class);
        map.put(Vertices.PROB_EXPONENTIAL, ExponentialVertex.class);
        map.put(Vertices.PROB_CAUCHY, CauchyVertex.class);
        map.put(Vertices.OP_ADDITION, AdditionVertex.class);
        map.put(Vertices.OP_DIFFERENCE, DifferenceVertex.class);
        map.put(Vertices.OP_DIVISION, DivisionVertex.class);
        map.put(Vertices.OP_MULTIPLICATION, MultiplicationVertex.class);
        map.put(Vertices.OP_POWER, PowerVertex.class);
        map.put(Vertices.OP_EXP, ExpVertex.class);
        map.put(Vertices.OP_SIGMOID, SigmoidVertex.class);
        map.put(Vertices.OP_SIN, SinVertex.class);
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

    public void transition(int num) {
        for (int i = 0; i < num; i++) {
            transition();
        }
    }

    private void add() {
        Vertices vertexToAdd = categoricalVertex.sample().scalar();
        Class vertexClass = vertexMap.get(vertexToAdd);
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
                        int randomIndex = (int) random.nextGamma(10, 0.5);
                        if (randomIndex >= vertices.size()) {
                            randomIndex = 0;
                        }
                        DoubleVertex choice = vertices.get(randomIndex);
                        params[i] = choice;
                    } else {
                        double value = random.nextDouble(0.0, 10.0);
                        DoubleVertex param = ConstantVertex.of(value);
                        params[i] = param;
                        vertices.add(param);
                    }
                }
                DoubleVertex newVertex = create(constructor, params);
                vertices.add(newVertex);
                sort(vertices);
            }
        }
    }

    private void delete() {

    }

    private void sort(List<DoubleVertex> vertices) {
        Collections.sort(vertices, new Comparator<DoubleVertex>(){
            public int compare(DoubleVertex o1, DoubleVertex o2){
                return o1.getChildren().size() - o2.getChildren().size();
            }
        });
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
