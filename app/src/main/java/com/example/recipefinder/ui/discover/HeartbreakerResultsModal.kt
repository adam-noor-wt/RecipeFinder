package com.example.recipefinder.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipefinder.R
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.RecipeViewModel
import com.example.recipefinder.data.Tag
import com.example.recipefinder.database.updateSavedRecipesInDatabase
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.Secondary
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartbreakerResultsModal(
    heartbreakerRecipes: List<Recipe>,
    currentlySaved: List<Recipe>,
    model: RecipeViewModel,
    open: Boolean,
    onClose: () -> Unit
) {
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Track which recipes have been saved to the broader list
    val savedToLibrary = remember(heartbreakerRecipes) {
        mutableStateMapOf<Int, Boolean>().apply {
            heartbreakerRecipes.forEach { recipe ->
                this[recipe.id] = currentlySaved.any { it.id == recipe.id }
            }
        }
    }

    if (open) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Primary.copy(alpha = 0.9f))
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .clickable(enabled = false) { } // Prevent click-through
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Your Heartbreaker Picks",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Tap a recipe to view details, or save them to your collection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "${heartbreakerRecipes.size} recipe(s) found",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Recipe list
                if (heartbreakerRecipes.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No recipes saved yet!",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Gray
                        )
                        Text(
                            text = "Swipe right on recipes to save them here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        itemsIndexed(heartbreakerRecipes) { index, recipe ->
                            val backgroundColor = if (index % 2 == 0) Primary else Color.White
                            val contentColor = if (index % 2 == 0) Color.White else Primary
                            val isSavedToLibrary = savedToLibrary[recipe.id] ?: false

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        5.dp,
                                        shape = MaterialTheme.shapes.medium,
                                        ambientColor = Color.Black,
                                        spotColor = Color.Black
                                    )
                                    .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                                    .clickable { selectedRecipe = recipe }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Recipe image
                                AsyncImage(
                                    model = recipe.imageUrl.ifEmpty { R.drawable.alfredo },
                                    contentDescription = "Recipe Image",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .padding(top = 2.dp, end = 2.dp),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                // Recipe info
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = recipe.title,
                                        color = contentColor,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (recipe.tags.isNotEmpty()) {
                                        val tags = Tag.sortedByPriority(recipe.tags).take(3)
                                        Row {
                                            tags.forEach { tag ->
                                                Text(
                                                    text = tag.displayName,
                                                    color = backgroundColor,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier
                                                        .padding(end = 6.dp)
                                                        .background(
                                                            color = contentColor.copy(alpha = 0.9f),
                                                            shape = MaterialTheme.shapes.extraSmall
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = recipe.description,
                                        color = contentColor.copy(alpha = 0.85f),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }

                                // Save/Unsave button
                                IconButton(
                                    onClick = {
                                        if (isSavedToLibrary) {
                                            // Remove from saved collection
                                            val updatedSaves = model.savedRecipes.value.filter { it.id != recipe.id }
                                            model.setSavedRecipes(updatedSaves)

                                            // Update in Firestore
                                            updateSavedRecipesInDatabase(
                                                Firebase.firestore,
                                                Firebase.auth.currentUser?.uid.toString(),
                                                updatedSaves
                                            )

                                            // Mark as unsaved locally
                                            savedToLibrary[recipe.id] = false
                                        } else {
                                            // Save to broader recipe collection
                                            val updatedSaves = model.savedRecipes.value + recipe
                                            model.setSavedRecipes(updatedSaves)

                                            // Update in Firestore
                                            updateSavedRecipesInDatabase(
                                                Firebase.firestore,
                                                Firebase.auth.currentUser?.uid.toString(),
                                                updatedSaves
                                            )

                                            // Mark as saved locally
                                            savedToLibrary[recipe.id] = true
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSavedToLibrary) Icons.Default.CheckCircle else Icons.Default.Add,
                                        contentDescription = if (isSavedToLibrary) "Remove from collection" else "Save to collection",
                                        tint = contentColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // Close button
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Secondary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(top = 16.dp)
                ) {
                    Text("Close")
                }
            }

            // Bottom sheet for recipe details
            if (selectedRecipe != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedRecipe = null },
                    sheetState = bottomSheetState,
                    modifier = Modifier.fillMaxHeight(0.9f),
                    dragHandle = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(
                                        color = Color.Gray,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                ) {
                    RecipeDetailsContent(recipe = selectedRecipe!!)
                }
            }
        }
    }
}
