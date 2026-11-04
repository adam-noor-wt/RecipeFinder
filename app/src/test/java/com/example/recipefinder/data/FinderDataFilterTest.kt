package com.example.recipefinder.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinderDataFilterTest {

    private fun ing(name: String) = Ingredient(0, name, 1.0, Unit.CUP)
    private fun recipe(id: Int, style: String = "", tags: List<Tag> = emptyList(), vararg ings: String) =
        Recipe(id, "R$id", "", 10, style = style, tags = tags, ingredients = ings.map(::ing))

    private val a = recipe(1, "Italian", listOf(Tag.PASTA, Tag.DINNER), "Flour", "Egg")
    private val b = recipe(2, "Thai", listOf(Tag.CURRY, Tag.SPICY, Tag.DINNER), "Rice", "Egg")
    private val c = recipe(3)
    private val all = listOf(a, b, c)

    @Test fun tagsAllMustMatch() {
        assertEquals(listOf(a, b), findRecipesWithTags(all, listOf(Tag.DINNER)))
        assertEquals(listOf(b), findRecipesWithTags(all, listOf(Tag.DINNER, Tag.SPICY)))
        assertTrue(findRecipesWithTags(all, listOf(Tag.VEGAN)).isEmpty())
    }

    @Test fun emptyTagsMatchesEverything() {
        assertEquals(all, findRecipesWithTags(all, emptyList()))
    }

    @Test fun emptyRecipeListYieldsEmpty() {
        assertTrue(findRecipesWithTags(emptyList(), listOf(Tag.DINNER)).isEmpty())
        assertTrue(findRecipesWithStyle(emptyList(), "x").isEmpty())
        assertTrue(findRecipesWithSomeIngredients(emptyList(), listOf("x")).isEmpty())
        assertTrue(findRecipesWithAllIngredients(emptyList(), listOf("x")).isEmpty())
    }

    @Test fun styleExactMatch() {
        assertEquals(listOf(a), findRecipesWithStyle(all, "Italian"))
        assertTrue(findRecipesWithStyle(all, "italian").isEmpty()) // case-sensitive
        assertEquals(listOf(c), findRecipesWithStyle(all, ""))
    }

    @Test fun someIngredients() {
        assertEquals(listOf(a, b), findRecipesWithSomeIngredients(all, listOf("Egg")))
        assertEquals(listOf(a, b), findRecipesWithSomeIngredients(all, listOf("Flour", "Rice")))
        assertTrue(findRecipesWithSomeIngredients(all, emptyList()).isEmpty())
    }

    @Test fun allIngredients() {
        assertEquals(listOf(a), findRecipesWithAllIngredients(all, listOf("Flour", "Egg")))
        assertEquals(listOf(a, b), findRecipesWithAllIngredients(all, listOf("Egg")))
        assertEquals(all, findRecipesWithAllIngredients(all, emptyList()))
    }

    @Test fun trimmedString() {
        assertEquals("2", 2.0.trimmedString())
        assertEquals("2.5", 2.5.trimmedString())
        assertEquals("0", 0.0.trimmedString())
        assertEquals("-3", (-3.0).trimmedString())
    }

    @Test fun fractionStringMixedAndNegativeEdge() {
        assertEquals("2 1/3", (2.0 + 1.0 / 3.0).fractionString())
        assertEquals("1/4", 0.25.fractionString())
        assertEquals("1", 1.0.fractionString())
    }

    @Test fun abbreviateUnitCoversAllUnits() {
        Unit.values().forEach { assertTrue(abbreviateUnit(it).isNotEmpty()) }
        assertEquals("g", abbreviateUnit(Unit.GRAM))
        assertEquals("tbsp", abbreviateUnit(Unit.TABLESPOON))
        assertEquals(" cup", abbreviateUnit(Unit.CUP))
    }

    @Test fun tagsSortedByPriorityDescending() {
        val sorted = Tag.sortedByPriority(listOf(Tag.VEGAN, Tag.BREAKFAST, Tag.PASTA))
        assertEquals(listOf(Tag.BREAKFAST, Tag.PASTA, Tag.VEGAN), sorted)
        assertTrue(Tag.sortedByPriority(emptyList()).isEmpty())
    }

    @Test fun tagPrioritiesAreUnique() {
        assertEquals(Tag.values().size, Tag.values().map { it.priority }.toSet().size)
    }

    @Test fun recipeDefaults() {
        val r = Recipe(1, "t", "d", 5)
        assertEquals("Anonymous", r.owner)
        assertTrue(r.steps.isEmpty() && r.tags.isEmpty() && r.ingredients.isEmpty())
        assertEquals(Step().title, "Something went wrong")
    }
}
