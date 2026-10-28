package com.example.recipefinder.data

import org.junit.Test
import org.junit.Assert.assertEquals

class FinderDataTest {

    @Test
    fun fractionStringWholeNumbers() {
        assertEquals("2", 2.0.fractionString())
        assertEquals("0", 0.0.fractionString())
    }

    @Test
    fun fractionStringSimpleFractions() {
        assertEquals("2/3", (2.0 / 3.0).fractionString())
        assertEquals("1/2", 0.5.fractionString())
        assertEquals("3/10", 0.3.fractionString())
        assertEquals("1 1/2", 1.5.fractionString())
    }

    @Test
    fun fractionStringFallsBackToDecimalBeyondDenominatorLimit() {
        // 2/19 has no denominator <= 10 close enough, so it should fall back to a 2-decimal string.
        val result = (2.0 / 19.0).fractionString()
        assertEquals("0.11", result)
    }
}
