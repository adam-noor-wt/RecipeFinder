package com.example.recipefinder.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeViewModelTest {
    private val r = Recipe(1, "t", "d", 1)

    @Test fun startsEmpty() {
        val vm = RecipeViewModel()
        assertTrue(vm.recipes.value.isEmpty())
        assertTrue(vm.savedRecipes.value.isEmpty())
        assertTrue(vm.folders.value.isEmpty())
    }

    @Test fun settersUpdateState() {
        val vm = RecipeViewModel()
        vm.setRecipes(listOf(r))
        vm.setSavedRecipes(listOf(r, r.copy(id = 2)))
        vm.setFolders(mapOf("Fav" to listOf(1, 2)))
        assertEquals(listOf(r), vm.recipes.value)
        assertEquals(2, vm.savedRecipes.value.size)
        assertEquals(listOf(1, 2), vm.folders.value["Fav"])
        vm.setRecipes(emptyList())
        assertTrue(vm.recipes.value.isEmpty())
    }
}
