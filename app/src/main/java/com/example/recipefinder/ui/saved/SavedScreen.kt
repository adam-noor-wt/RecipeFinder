package com.example.recipefinder.ui.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.Tag
import com.example.recipefinder.ui.search.AddSectionButton
import com.example.recipefinder.ui.search.IngredientChip

import com.example.recipefinder.ui.search.tagLabel
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.RecipeFinderTheme

private data class IngredientGroup(val displayName: String, val variants: Set<String>)
private const val MAX_INGREDIENT_SUGGESTIONS = 8
private const val INGREDIENT_NAME_CHAR_LIMIT = 24

private fun groupIngredientNames(names: List<String>): List<IngredientGroup> =
    names.groupBy { com.example.recipefinder.data.normalizeIngredientKey(it) }
        .map { (_, variants) ->
            val displayName = variants.groupingBy { it }.eachCount()
                .maxByOrNull { it.value }!!.key
            IngredientGroup(displayName, variants.toSet())
        }
@Composable
fun SavedScreen(
    recipes: List<Recipe> = emptyList(),
    onRecipeClick: (Recipe) -> Unit = {}
)
{
    val searchQuery = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val ingredients = recipes.flatMap { it.ingredients.map { ingredient -> ingredient.name } }.distinct()
    val ingredientGroups = androidx.compose.runtime.remember(recipes) {
        groupIngredientNames(recipes.flatMap { it.ingredients.map { ingredient -> ingredient.name } })
    }
    val selectedIngredients = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateListOf<IngredientGroup>()
    }
    val selectedTags = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateListOf<Tag>()
    }
    // Filter recipes based on search query and selected ingredients from detailed search
    // Filter recipes based on search query, selected ingredients, and selected tags from detailed search
    val filteredRecipes = recipes.filter { recipe ->
        (searchQuery.value.isBlank() ||
                recipe.title.contains(searchQuery.value, ignoreCase = true) ||
                recipe.description.contains(searchQuery.value, ignoreCase = true)) &&
                selectedIngredients.all { group -> recipe.ingredients.any { ingredient -> ingredient.name in group.variants } } &&
                selectedTags.all { tag -> tag in recipe.tags }
    }
    Column {
        Column (
            modifier = Modifier.background(color = Primary)
        ){
            androidx.compose.material3.OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                label = { Text("Search recipes") },
                shape = RoundedCornerShape(15.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)

            )
            val showDetailedSearch = androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(false)
            }
            Text(
                text = if (showDetailedSearch.value) "Hide Detailed Search \u25B2" else "Show Detailed Search \u25BC",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 12.dp, top = 4.dp, bottom = 8.dp)
                    .clickable { showDetailedSearch.value = !showDetailedSearch.value }
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = showDetailedSearch.value,
                enter = androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    if (selectedIngredients.isNotEmpty()) {
                        FlowRow(modifier = Modifier.padding(bottom = 4.dp)) {
                            selectedIngredients.forEach { group ->
                                IngredientChip(
                                    text = group.displayName,
                                    onRemove = { selectedIngredients.remove(group) }
                                )
                            }
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            val showIngredientMenu = androidx.compose.runtime.remember {
                                androidx.compose.runtime.mutableStateOf(false)
                            }
                            Box(modifier = Modifier.padding(4.dp)) {
                                AddSectionButton("Filter by ingredient") { showIngredientMenu.value = true }
                                DropdownMenu(
                                    expanded = showIngredientMenu.value,
                                    onDismissRequest = { showIngredientMenu.value = false },
                                    modifier = Modifier
                                        .heightIn(max = 280.dp)
                                        .fillMaxWidth(.7f)
                                ) {
                                    val ingredientQuery = androidx.compose.runtime.remember {
                                        androidx.compose.runtime.mutableStateOf("")
                                    }
                                    androidx.compose.material3.OutlinedTextField(
                                        value = ingredientQuery.value,
                                        onValueChange = { ingredientQuery.value = it },
                                        placeholder = { Text("Search ingredients...") },
                                        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    )
                                    val suggestions = ingredientGroups
                                        .filter { it !in selectedIngredients && it.displayName.contains(ingredientQuery.value, ignoreCase = true) }
                                        .take(MAX_INGREDIENT_SUGGESTIONS)
                                    if (suggestions.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("No matching ingredients", color = Color.Gray) },
                                            onClick = {},
                                            enabled = false
                                        )
                                    } else {
                                        suggestions.forEach { suggestion ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = suggestion.displayName.take(INGREDIENT_NAME_CHAR_LIMIT)
                                                            .let { if (it.length < suggestion.displayName.length) "$it…" else it },
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
                                                onClick = {
                                                    selectedIngredients.add(suggestion)
                                                    ingredientQuery.value = ""
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (selectedTags.isNotEmpty()) {
                                FlowRow(modifier = Modifier.padding(bottom = 4.dp)) {
                                    selectedTags.forEach { tag ->
                                        IngredientChip(
                                            text = tagLabel(tag),
                                            onRemove = { selectedTags.remove(tag) }
                                        )
                                    }
                                }
                            }
                            val showTagMenu = androidx.compose.runtime.remember {
                                androidx.compose.runtime.mutableStateOf(false)
                            }
                            Box(modifier = Modifier.padding(4.dp)) {
                                AddSectionButton("Filter by tag") { showTagMenu.value = true }
                                DropdownMenu(
                                    expanded = showTagMenu.value,
                                    onDismissRequest = { showTagMenu.value = false },
                                    modifier = Modifier
                                        .heightIn(max = 280.dp)
                                        .fillMaxWidth(.7f)
                                ) {
                                    val tagQuery = androidx.compose.runtime.remember {
                                        androidx.compose.runtime.mutableStateOf("")
                                    }
                                    androidx.compose.material3.OutlinedTextField(
                                        value = tagQuery.value,
                                        onValueChange = { tagQuery.value = it },
                                        placeholder = { Text("Search tags...") },
                                        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    )
                                    Tag.entries
                                        .filter { it !in selectedTags && tagLabel(it).contains(tagQuery.value, ignoreCase = true) }
                                        .forEach { tag ->
                                            DropdownMenuItem(
                                                text = { Text(tagLabel(tag)) },
                                                onClick = {
                                                    selectedTags.add(tag)
                                                    tagQuery.value = ""
                                                }
                                            )
                                        }
                                }
                            }
                        }
                    }


                }

            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ){
            itemsIndexed(filteredRecipes) { index, recipe ->
                val backgroundColor = if (index % 2 == 0) Primary else androidx.compose.ui.graphics.Color.White
                val contentColor = if (index % 2 == 0) androidx.compose.ui.graphics.Color.White else Primary

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(5.dp, shape = MaterialTheme.shapes.medium, ambientColor = androidx.compose.ui.graphics.Color.Black, spotColor = androidx.compose.ui.graphics.Color.Black)
                        .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                        .clickable { onRecipeClick(recipe) }
                        .padding(8.dp),
                ) {
                    // Placeholder for image
                    AsyncImage(
                        model = recipe.imageUrl.ifEmpty { com.example.recipefinder.R.drawable.alfredo },
                        contentDescription = "Recipe Image",
                        modifier = Modifier.size(64.dp).padding(top = 2.dp, end = 2.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = recipe.title,
                            color = contentColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (recipe.tags.isNotEmpty()) {
                            val tags: List<Tag> = Tag.sortedByPriority(recipe.tags).take(3) // Limit to 3 tags for display
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
                        val descLines: String = recipe.description
                        Text(
                            text = descLines,
                            color = contentColor.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun SavedScreenPreview() {
    val example: MutableList<Recipe>  = mutableListOf(
        Recipe(1, "Example Recipe 1",  "Description of Example Recipe 1", 40, tags = listOf(Tag.SOUP, Tag.LUNCH, Tag.PASTA, Tag.DAIRY_FREE, Tag.TOFU, Tag.GLUTEN_FREE)),
        Recipe(2, "Example Recipe 2", "Description of Example Recipe 2", 80, tags = listOf(Tag.BREAKFAST, Tag.VEGAN)),
        Recipe(3, "Example Recipe 3", "Description of Example Recipe 3", 120, tags = listOf(Tag.DESSERT)),
        Recipe(4, "Example Recipe 4", "Description of Example Recipe 4", 20),
        Recipe(5, "Example Recipe 5", "Description of Example Recipe 5", 15),
    )
    RecipeFinderTheme {
        SavedScreen(example, {})
    }

}