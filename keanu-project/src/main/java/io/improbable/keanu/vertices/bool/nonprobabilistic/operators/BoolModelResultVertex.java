package io.improbable.keanu.vertices.bool.nonprobabilistic.operators;

import io.improbable.keanu.tensor.bool.BooleanTensor;
import io.improbable.keanu.vertices.NonProbabilistic;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexLabel;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.ModelResultVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.multiple.ModelVertex;

public class BoolModelResultVertex extends BoolVertex implements ModelResultVertex<BooleanTensor>, NonProbabilistic<BooleanTensor> {

    private ModelVertex model;
    private VertexLabel label;
    private boolean hasValue;

    public BoolModelResultVertex(ModelVertex model, VertexLabel label) {
        this.model = model;
        this.label = label;
        this.hasValue = false;
        setParents((Vertex) model);
    }

    @Override
    public BooleanTensor getValue() {
        if (!hasValue) {
            model.calculate();
        }
        return BooleanTensor.scalar(model.getBooleanModelOutputValue(label));
    }

    @Override
    public BooleanTensor sample(KeanuRandom random) {
        return BooleanTensor.scalar(model.getBooleanModelOutputValue(label));
    }

    @Override
    public BooleanTensor calculate() {
        hasValue = true;
        return BooleanTensor.scalar(model.getBooleanModelOutputValue(label));
    }
}
