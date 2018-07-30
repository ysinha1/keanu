package io.improbable.research

import io.improbable.keanu.tensor.dbl.DoubleTensor
import io.improbable.keanu.vertices.Vertex
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.DoubleUnaryOpLambda

class ModelVertex : DoubleUnaryOpLambda<DoubleTensor> {
    val model : AbstractModel

    constructor(inVertex : Vertex<DoubleTensor>, model : AbstractModel = AbstractModel(inVertex.value), name: String = "") :
        super(inVertex, {
            model.setStateFromTensor(it)
            model.step(it)
            model.getStateAsTensor()
        }, {
            model.calculateDualNumber(it[inVertex])
        }) {
        this.model = model
    }


}