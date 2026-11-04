package com.example.recipefinder.database

import com.example.recipefinder.data.Ingredient
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.Step
import com.example.recipefinder.data.Tag
import com.example.recipefinder.data.Unit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseConvertersTest {

    private val recipe = Recipe(
        id = 42, title = "Pasta", description = "desc", preparationTime = 15,
        steps = listOf(Step(1, "Boil", "Boil water")),
        imageUrl = "http://img", ingredients = listOf(Ingredient(1, "Flour", 2.5, Unit.CUP, "sifted")),
        style = "Italian", tags = listOf(Tag.PASTA, Tag.GLUTEN_FREE), owner = "uid1"
    )

    @Test fun roundTripPreservesEverything() {
        assertEquals(recipe, safeRecipeConverter(recipeConverter(recipe)))
    }

    @Test fun toSafeConvertsEnumsToStrings() {
        val safe = recipeConverter(recipe)
        assertEquals("CUP", safe.ingredients[0].unit)
        assertEquals(listOf("PASTA", "GLUTEN_FREE"), safe.tags.map { it.tag })
    }

    @Test fun emptyCollectionsRoundTrip() {
        val r = Recipe(1, "t", "d", 0)
        assertEquals(r, safeRecipeConverter(recipeConverter(r)))
    }

    @Test fun safeDefaultsMatchFirestoreExpectations() {
        val s = SafeRecipe()
        assertEquals("Something went wrong", s.title)
        assertEquals("Anonymous", s.owner)
        assertTrue(s.ingredients.isEmpty())
    }

    // NOTE: invalid enum strings in Firestore (e.g. a removed Tag or empty unit from SafeIngredient()
    // defaults) make the converter throw IllegalArgumentException, which would crash
    // readRecipesFromDatabase. Behaviour is pinned here; flagged for later review.
    @Test(expected = IllegalArgumentException::class)
    fun unknownUnitThrows() {
        safeRecipeConverter(SafeRecipe(ingredients = listOf(SafeIngredient(unit = "BOGUS"))))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unknownTagThrows() {
        safeRecipeConverter(SafeRecipe(tags = listOf(SafeTag("BOGUS"))))
    }

    @Test(expected = IllegalArgumentException::class)
    fun defaultSafeIngredientHasInvalidUnit() {
        safeRecipeConverter(SafeRecipe(ingredients = listOf(SafeIngredient())))
    }
}
