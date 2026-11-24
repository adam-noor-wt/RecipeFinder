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
    private val _follows = MutableStateFlow<List<String>>(emptyList())
    val follows: StateFlow<List<String>> = _follows
    fun setFollows(value: List<String>) { _follows.value = value }
    private val _viewed = MutableStateFlow<Set<Int>>(emptySet())
    /** Ids of followed users' recipes the current user has already seen (by opening the creator's profile). */
    val viewed: StateFlow<Set<Int>> = _viewed
    fun setViewed(value: Set<Int>) { _viewed.value = value }
    private val _profiles = MutableStateFlow<Map<String, PublicProfile>>(emptyMap())
    val profiles: StateFlow<Map<String, PublicProfile>> = _profiles
    fun putProfiles(value: Map<String, PublicProfile>) { _profiles.value = _profiles.value + value }
    private val _socialLoaded = MutableStateFlow(false)
    val socialLoaded: StateFlow<Boolean> = _socialLoaded
    fun setSocialLoaded(value: Boolean) { _socialLoaded.value = value }
    fun clearSocial() {
        _follows.value = emptyList(); _viewed.value = emptySet(); _profiles.value = emptyMap(); _socialLoaded.value = false
    }
}
