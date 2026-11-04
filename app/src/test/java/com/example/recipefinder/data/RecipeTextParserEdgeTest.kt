package com.example.recipefinder.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeTextParserEdgeTest {
    @Test fun emptyAndBlankInput() {
        assertEquals(ParsedRecipe(), parseRecipeText(""))
        assertEquals(ParsedRecipe(), parseRecipeText("  \n \n"))
    }

    @Test fun hoursAndMinutesSummed() {
        val r = parseRecipeText("Soup\nTakes 1 hour and 15 mins")
        assertEquals(75, r.preparationTime)
    }

    @Test fun bulletedIngredientsParsed() {
        val r = parseRecipeText("Title\n- 2 cups milk\n• 100 g sugar")
        assertEquals(2, r.ingredients.size)
        assertEquals(Unit.GRAM, r.ingredients[1].unit)
        assertEquals("Milk", r.ingredients[0].name)
    }

    @Test fun zeroDenominatorIsRejected() {
        val r = parseRecipeText("Title\n1/0 cup flour")
        assertTrue(r.ingredients.isEmpty())
    }

    @Test fun longTitleTruncatedTo80() {
        assertEquals(80, parseRecipeText("a".repeat(200)).title.length)
    }

    @Test fun nonConsecutiveNumberingBreaksRun() {
        val r = parseRecipeText("T\n1. First\n3. Third")
        assertEquals(1, r.steps.size)
    }

    @Test fun stepTitleSplitsOnFirstSentence() {
        val r = parseRecipeText("T\n1. Mix well. Then rest.\n2. Bake")
        assertEquals("Mix well", r.steps[0].title)
        assertEquals("Mix well. Then rest.", r.steps[0].description)
        assertEquals("Bake", r.steps[1].title)
    }
}
