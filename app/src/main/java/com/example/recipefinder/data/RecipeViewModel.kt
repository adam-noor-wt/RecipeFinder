package com.example.recipefinder.data
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecipeViewModel : ViewModel() {
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes
    private val _savedRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val savedRecipes: StateFlow<List<Recipe>> = _savedRecipes
    fun setRecipes(newRecipes: List<Recipe>) {
        _recipes.value = newRecipes
    }
    fun setSavedRecipes(newRecipes: List<Recipe>) {
        _savedRecipes.value = newRecipes
    }
    private val _hasLoaded = MutableStateFlow(false)
    /** True once the first fetch has finished (successfully or not). */
    val hasLoaded: StateFlow<Boolean> = _hasLoaded
    fun setHasLoaded(loaded: Boolean) {
        _hasLoaded.value = loaded
    }
    private val _folders = MutableStateFlow<Map<String, List<Int>>>(emptyMap())
    val folders: StateFlow<Map<String, List<Int>>> = _folders
    fun setFolders(newFolders: Map<String, List<Int>>) {
        _folders.value = newFolders
    }
}
