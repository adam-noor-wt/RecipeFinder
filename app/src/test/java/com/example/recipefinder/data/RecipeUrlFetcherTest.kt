package com.example.recipefinder.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RecipeUrlFetcherTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun fetch(html: String) = runBlocking {
        val f = tmp.newFile()
        f.writeText(html)
        RecipeUrlFetcher().fetchRecipe(f.toURI().toURL().toString())
    }

    private fun page(json: String) = """<html><script type="application/ld+json">$json</script></html>"""

    @Test fun parsesRecipeJsonLd() {
        val r = fetch(page("""{"@type":"Recipe","name":"Cake","description":"Yum","prepTime":"PT1H30M",
            "recipeIngredient":["2 cups flour","1/2 tsp salt","3 eggs","", "pinch"],
            "recipeInstructions":[{"text":"Mix"},"Bake",{"text":" "}]}""")).getOrThrow()
        assertEquals("Cake", r.title)
        assertEquals(90, r.preparationTime)
        assertEquals(4, r.ingredients.size)
        assertEquals(Unit.CUP, r.ingredients[0].unit)
        assertEquals(2.0, r.ingredients[0].quantity, 0.0)
        assertEquals("flour", r.ingredients[0].name)
        assertEquals(0.5, r.ingredients[1].quantity, 0.0)
        assertEquals(Unit.TEASPOON, r.ingredients[1].unit)
        assertEquals(2, r.steps.size)
        assertEquals(2, r.steps[1].index)
        assertEquals("Bake", r.steps[1].description)
    }

    @Test fun parsesGraphAndArrayForms() {
        val body = """{"@type":"Recipe","name":"X","recipeIngredient":["1 g sugar"]}"""
        assertEquals("X", fetch(page("""{"@graph":[{"@type":"WebSite"},$body]}""")).getOrThrow().title)
        assertEquals("X", fetch(page("[$body]")).getOrThrow().title)
    }

    @Test fun failuresAreReportedNotThrown() {
        assertTrue(fetch("<html></html>").isFailure)
        assertTrue(fetch(page("not json")).isFailure)
        assertTrue(fetch(page("""{"@type":"Recipe","name":"Empty"}""")).isFailure)
        assertTrue(fetch(page("""{"@type":"Recipe","name":"","recipeIngredient":["1 cup a"]}""")).isFailure)
        assertTrue(runBlocking { RecipeUrlFetcher().fetchRecipe("not a url") }.isFailure)
    }

    @Test fun unrecognisedUnitStaysInName() {
        val r = fetch(page("""{"@type":"Recipe","name":"X","recipeIngredient":["2 large onions","salt"]}""")).getOrThrow()
        assertEquals(Unit.PIECE, r.ingredients[0].unit)
        assertEquals("large onions", r.ingredients[0].name)
        assertEquals(1.0, r.ingredients[1].quantity, 0.0)
        assertEquals("salt", r.ingredients[1].name)
    }
}
