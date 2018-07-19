package io.improbable.keanu.research

import junit.framework.Assert.assertEquals
import org.junit.Test

class Array2DStackingTest {

    @Test
    fun horizontalStackingTest() {
        var leftArray = Array2D(10, 7, {_, _ -> 1})
        var rightArray = Array2D(20, 7, {_, _ -> 1})
        var hStack = leftArray.horizontallyStack(rightArray)
        assertEquals(30, hStack.iSize())
        assertEquals(7, hStack.jSize())
        for (i in 0 until hStack.iSize()) {
            for (j in 0 until hStack.jSize()) {
                assertEquals(1, hStack[i, j])
            }
        }
    }
    
    @Test 
    fun verticalStackingTest() {
        var topArray = Array2D(7, 10, {_, _ -> 1})
        var bottomArray = Array2D(7, 20, {_, _ -> 1})
        var vStack = topArray.verticallyStack(bottomArray)
        assertEquals(30, vStack.jSize())
        assertEquals(7, vStack.iSize())
        for (i in 0 until vStack.iSize()) {
            for (j in 0 until vStack.jSize()) {
                assertEquals(1, vStack[i, j])
            }
        }
    }

}

