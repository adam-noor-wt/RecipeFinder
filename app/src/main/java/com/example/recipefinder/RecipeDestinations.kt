package com.example.recipefinder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface RecipeDestination {
    val icon: ImageVector
    val route: String
}

/**
 * Recipe app navigation destinations
 */
object Home : RecipeDestination {
    override val icon = Icons.Filled.Home // Placeholder icon, not used in navigation
    override val route = "home"
}

object Saved : RecipeDestination {
    override val icon = Icons.AutoMirrored.Filled.List // Placeholder icon, not used in navigation
    override val route = "saved"
}

object Search : RecipeDestination {
    override val icon = Icons.Filled.Search
    override val route = "search"
}

object Discover : RecipeDestination {
    override val icon = Icons.Outlined.Explore
    override val route = "discover"
}

object Settings : RecipeDestination {
    override val icon = Icons.Filled.Settings
    override val route = "settings"
}

object CreateRecipe : RecipeDestination {
    override val icon = Icons.Filled.Add // Placeholder icon, not used in navigation
    override val route = "create_recipe"
}

object SingleRecipe : RecipeDestination {
    override val icon = Icons.Outlined.MailOutline // Placeholder icon, not used in navigation
    override val route = "single_recipe"
    const val recipeIdArg = "recipe_id"
    val routeWithArgs = "${route}/{${recipeIdArg}}"
    val arguments = listOf(
        navArgument(recipeIdArg) { type = NavType.IntType }
    )
}

object EditRecipe : RecipeDestination {
    override val icon = Icons.Outlined.MailOutline // Placeholder icon, not used in navigation
    override val route = "edit_recipe"
    const val recipeIdArg = "recipe_id"
    val routeWithArgs = "${route}/{${recipeIdArg}}"
    val arguments = listOf(
        navArgument(recipeIdArg) { type = NavType.IntType }
    )
}

val recipeDestinations = listOf(Home, Saved, Search, Discover, Settings)