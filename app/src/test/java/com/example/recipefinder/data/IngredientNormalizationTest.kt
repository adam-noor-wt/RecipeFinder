package com.example.recipefinder.data

import org.junit.Test
import org.junit.Assert.assertEquals

class IngredientNormalizationTest {

    @Test
    fun mergesSimplePlurals() {
        assertEquals(normalizeIngredientKey("Tomato"), normalizeIngredientKey("Tomatoes"))
        assertEquals(normalizeIngredientKey("Egg"), normalizeIngredientKey("Eggs"))
    }

    @Test
    fun isCaseAndWhitespaceInsensitive() {
        assertEquals("tomato", normalizeIngredientKey("  Tomato  "))
        assertEquals("tomato", normalizeIngredientKey("TOMATO"))
    }

    @Test
    fun doesNotStripWordsEndingInDoubleS() {
        assertEquals("molasses", normalizeIngredientKey("Molasses"))
    }

    @Test
    fun leavesAlreadySingularWordsUnchanged() {
        assertEquals("flour", normalizeIngredientKey("Flour"))
        assertEquals("salt", normalizeIngredientKey("Salt"))
    }

    @Test
    fun doesNotMergeUnrelatedNames() {
        assertEquals(normalizeIngredientKey("Tomato"), normalizeIngredientKey("Tomato"))
        org.junit.Assert.assertNotEquals(normalizeIngredientKey("Tomato"), normalizeIngredientKey("Tomato Sauce"))
    }
}
