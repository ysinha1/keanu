package io.improbable.research

import org.junit.Test

class TestDifferentials {
    @Test
    fun testDifferentials() {
        val model = AbstractModel(96.0, 4.0, 0.01)
        for(step in 1..40) {
            model.step()
            val out = "$step ${model.rhoS} ${model.rhoI} ${model.rhoR}\n"
            print(out)
        }

    }
}