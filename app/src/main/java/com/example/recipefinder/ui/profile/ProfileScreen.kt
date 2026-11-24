package com.example.recipefinder.ui.profile

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.ui.settings.SettingsScreen

/** Read-only version of the Settings page for another user. [onToggleFollow] is null when following isn't allowed. */
@Composable
fun ProfileScreen(
    uid: String,
    displayName: String,
    recipes: List<Recipe>,
    isFollowing: Boolean,
    onToggleFollow: (() -> Unit)?,
    onRecipeClick: (Recipe) -> Unit
) {
    SettingsScreen(
        displayName = displayName,
        onEditDisplayName = {},
        signOut = {},
        recipes = recipes,
        onRecipeClick = onRecipeClick,
        savedRecipes = emptyList(),
        readOnly = true,
        uid = uid,
        headerExtra = {
            if (onToggleFollow != null) {
                if (isFollowing) OutlinedButton(onClick = onToggleFollow) { Text("Unfollow") }
                else Button(onClick = onToggleFollow) { Text("Follow") }
            }
        }
    )
}
