package io.improbable.research

import io.improbable.keanu.research.vertices.DoubleTernaryOpLambda
import io.improbable.keanu.tensor.dbl.DoubleTensor
import io.improbable.keanu.vertices.Vertex
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.DoubleUnaryOpLambda

//class ModelVertexNoTensor : DoubleTernaryOpLambda<DoubleTensor, DoubleTensor, DoubleTensor> {
//    val model: AbstractModel
//
//    constructor(s: Vertex<DoubleTensor>, i: Vertex<DoubleTensor>, r: Vertex<DoubleTensor>, model: AbstractModel = AbstractModel(s.value, i.value, r.value)) :
//        super(intArrayOf(1, 3), s, i, r,
//            { s: DoubleTensor, i: DoubleTensor, r: DoubleTensor ->
//                model.setState(s.scalar(), i.scalar(), r.scalar())
//                model.step()
//                model.getStateAsTensor()
//            },
//            { dualMap: Map<out Vertex<out Any>, DualNumber> ->
//                model.calculateDualNumber(dualMap.get(s)!!, dualMap[i]!!, dualMap[r]!!, s.id, i.id, r.id)
//            }) {
//
//        this.model = model
//    }
//
//
//}