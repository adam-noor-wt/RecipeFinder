package com.example.recipefinder.data

import org.junit.Test
import org.junit.Assert.assertEquals

class RecipeTextParserTest {

    @Test
    fun parsesTypicalRecipe() {
        val text = """
            Tomato Pasta
            A quick weeknight dinner. Prep time: 10 minutes

            2 cups flour
            1.5 tbsp olive oil
            1/2 cup parmesan
            salt to taste

            1. Boil water and cook pasta until al dente.
            2. Heat oil in a pan and add garlic.
            3. Toss pasta with sauce and serve hot.
        """.trimIndent()

        val result = parseRecipeText(text)

        println("title=${result.title}")
        println("description=${result.description}")
        println("prepTime=${result.preparationTime}")
        println("ingredients=${result.ingredients}")
        println("steps=${result.steps}")

        assertEquals("Tomato Pasta", result.title)
        assertEquals(10, result.preparationTime)
        assertEquals(3, result.ingredients.size)
        assertEquals(3, result.steps.size)
        assertEquals(1, result.steps[0].index)
        assertEquals(2, result.steps[1].index)
        assertEquals(3, result.steps[2].index)
    }

    @Test
    fun parsesFractionsAndSynonyms() {
        val text = """
            Simple Sauce
            3/4 cup tomato sauce
            2 tbsp butter
            1. Mix everything together.
            2. Simmer for ten minutes.
        """.trimIndent()

        val result = parseRecipeText(text)
        println("ingredients=${result.ingredients}")
        assertEquals(2, result.ingredients.size)
        assertEquals(0.75, result.ingredients[0].quantity, 0.001)
        assertEquals(Unit.CUP, result.ingredients[0].unit)
        assertEquals(2.0, result.ingredients[1].quantity, 0.001)
        assertEquals(Unit.TABLESPOON, result.ingredients[1].unit)
    }

    @Test
    fun handlesUnstructuredTextGracefully() {
        val text = "Just some random notes with no structure at all."
        val result = parseRecipeText(text)
        println("title=${result.title} ingredients=${result.ingredients} steps=${result.steps}")
        assertEquals(0, result.steps.size)
        assertEquals(0, result.ingredients.size)
    }
}
