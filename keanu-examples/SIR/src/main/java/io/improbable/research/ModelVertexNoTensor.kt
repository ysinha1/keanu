package io.improbable.research

import io.improbable.keanu.research.vertices.DoubleTernaryOpLambda
import io.improbable.keanu.tensor.dbl.DoubleTensor
import io.improbable.keanu.vertices.Vertex
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.DoubleUnaryOpLambda

//class ModelVertexNoTensor : DoubleTernaryOpLambda<DoubleTensor, DoubleTensor, DoubleTensor> {
//    val model: AbstractModel
//
//    constructor(S: Vertex<DoubleTensor>, I: Vertex<DoubleTensor>, R: Vertex<DoubleTensor>, model: AbstractModel = AbstractModel(S.value, I.value, R.value)) :
//        super(intArrayOf(1, 3), s, I, R,
//            { s: DoubleTensor, I: DoubleTensor, R: DoubleTensor ->
//                model.setState(s.scalar(), I.scalar(), R.scalar())
//                model.step()
//                model.getStateAsTensor()
//            },
//            { dualMap: Map<out Vertex<out Any>, DualNumber> ->
//                model.calculateDualNumber(dualMap.get(S)!!, dualMap[I]!!, dualMap[R]!!, S.id, I.id, R.id)
//            }) {
//
//        this.model = model
//    }
//
//
//}