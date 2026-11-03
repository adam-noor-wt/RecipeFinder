package com.example.recipefinder.ui.search

import android.annotation.SuppressLint
import androidx.compose.animation.EnterTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.Tag
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.RecipeFinderTheme
import com.example.recipefinder.ui.theme.Secondary
import com.example.recipefinder.ui.theme.Typography

private const val MAX_INGREDIENT_SUGGESTIONS = 8
private const val INGREDIENT_NAME_CHAR_LIMIT = 24

fun tagLabel(tag: Tag): String = tag.displayName

// Estimates how many tags fit into a fixed-size area, packing them into rows the way
// FlowRow will, so we only compose as many pills as the reserved bottom-quarter zone
// can actually hold instead of letting them overflow it.
private fun packTagsToFit(
    tags: List<Tag>,
    maxWidthDp: Float,
    maxHeightDp: Float,
    pillHeightDp: Float = 22f,
    rowSpacingDp: Float = 6f,
    charWidthDp: Float = 7f,
    hPaddingDp: Float = 12f,
    pillSpacingDp: Float = 6f
): List<Tag> {
    if (maxWidthDp <= 0f || maxHeightDp <= 0f) return emptyList()
    val maxRows = ((maxHeightDp + rowSpacingDp) / (pillHeightDp + rowSpacingDp)).toInt().coerceAtLeast(1)
    val result = mutableListOf<Tag>()
    var rows = 1
    var rowWidth = 0f
    for (tag in tags) {
        val pillWidth = tagLabel(tag).length * charWidthDp + hPaddingDp
        val addedWidth = if (rowWidth == 0f) pillWidth else pillWidth + pillSpacingDp
        if (rowWidth + addedWidth > maxWidthDp) {
            if (rows >= maxRows) break
            rows++
            rowWidth = pillWidth
        } else {
            rowWidth += addedWidth
        }
        result.add(tag)
    }
    return result
}

@Composable
private fun TagPill(tag: Tag) {
    Box(
        modifier = Modifier
            .height(22.dp)
            .background(color = Secondary, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tagLabel(tag),
            color = Color.White,
            fontSize = 10.sp,
            softWrap = false,
            maxLines = 1
        )
    }
}

// Groups ingredient names that normalize to the same key (see normalizeIngredientKey) so
// near-duplicate free-text entries (e.g. "Tomato"/"Tomatoes") collapse into one filter.
private data class IngredientGroup(val displayName: String, val variants: Set<String>)

private fun groupIngredientNames(names: List<String>): List<IngredientGroup> =
    names.groupBy { com.example.recipefinder.data.normalizeIngredientKey(it) }
        .map { (_, variants) ->
            val displayName = variants.groupingBy { it }.eachCount()
                .maxByOrNull { it.value }!!.key
            IngredientGroup(displayName, variants.toSet())
        }

@Composable
fun AddSectionButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = BorderStroke(1.dp, Color.White),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
        Text(text)
    }
}

@Composable
fun SuggestedSectionButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
        border = BorderStroke(1.dp, Color.LightGray),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
        Text(text)
    }
}

@Composable
fun IngredientChip(text: String, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.padding(4.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Text(
                text = text.take(INGREDIENT_NAME_CHAR_LIMIT)
                    .let { if (it.length < text.length) "$it…" else it },
                color = Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = Primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SearchScreen(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit = {}
)
{
    val searchQuery = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val ingredientGroups = androidx.compose.runtime.remember(recipes) {
        groupIngredientNames(recipes.flatMap { it.ingredients.map { ingredient -> ingredient.name } })
    }
    val selectedIngredients = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateListOf<IngredientGroup>()
    }
    val selectedTags = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateListOf<Tag>()
    }
    // Filter recipes based on search query, selected ingredients, and selected tags from detailed search
    val filteredRecipes = recipes.filter { recipe ->
        (searchQuery.value.isBlank() ||
            recipe.title.contains(searchQuery.value, ignoreCase = true) ||
            recipe.description.contains(searchQuery.value, ignoreCase = true)) &&
            selectedIngredients.all { group -> recipe.ingredients.any { ingredient -> ingredient.name in group.variants } } &&
            selectedTags.all { tag -> tag in recipe.tags }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    )
    {
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
                                    Row() {
                                        AddSectionButton("Filter by tag") { showTagMenu.value = true }
                                        if(selectedTags.isEmpty()) {
                                                SuggestedSectionButton("Dinner") {
                                                    selectedTags.add(
                                                        Tag.DINNER
                                                    )
                                                }
                                                SuggestedSectionButton("Lunch") {
                                                    selectedTags.add(
                                                        Tag.LUNCH
                                                    )
                                                }
                                            }
                                    }
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
            // Display recipes in a grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Iterate through filtered recipes and display each recipe in a card
                items(filteredRecipes) { recipe ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 2.dp,
                        color = Primary.copy(alpha = 0.85f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.9f)
                            .clickable { onRecipeClick(recipe) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            AsyncImage(
                                model = recipe.imageUrl.ifEmpty { com.example.recipefinder.R.drawable.alfredo },
                                contentDescription = recipe.title,
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.5f
                            )
                            Column(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(0.75f)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    // The title of the recipe
                                    Text(
                                        text = recipe.title,
                                        style = Typography.titleLarge.copy(
                                            fontSize = 18.sp,
                                            shadow = Shadow(
                                                color = Color.Black,
                                                offset = Offset(0.0f, 0.0f),
                                                blurRadius = 15f
                                            )
                                        ),
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        color = Color.White
                                    )
                                    Row {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Time to cook",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .size(16.dp)
                                                .then(
                                                    Modifier
                                                        .graphicsLayer {
                                                            shadowElevation = 8f
                                                            clip = false
                                                        }
                                                )
                                        )
                                        Text(
                                            text = "${recipe.preparationTime} min",
                                            style = Typography.labelLarge.copy(
                                                fontSize = 14.sp,
                                                shadow = Shadow(
                                                    color = Color.Black,
                                                    offset = Offset(0.0f, 0.0f),
                                                    blurRadius = 15f
                                                )
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                                // Bottom quarter of the card: as many tag pills as fit,
                                // packed into rows and centered both horizontally and vertically.
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .weight(0.25f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val sortedTags = Tag.sortedByPriority(recipe.tags)
                                    val visibleTags = remember(sortedTags, maxWidth, maxHeight) {
                                        packTagsToFit(sortedTags, maxWidth.value, maxHeight.value)
                                    }
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        visibleTags.forEach { tag -> TagPill(tag) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    val example: MutableList<Recipe>  = mutableListOf(
        Recipe(1, "Example Recipe 1",  "Description of Example Recipe 1", 40, tags = listOf(Tag.SOUP, Tag.LUNCH, Tag.PASTA, Tag.DAIRY_FREE, Tag.TOFU, Tag.GLUTEN_FREE)),
        Recipe(2, "Example Recipe 2", "Description of Example Recipe 2", 80, tags = listOf(Tag.BREAKFAST, Tag.VEGAN)),
        Recipe(3, "Example Recipe 3", "Description of Example Recipe 3", 120, tags = listOf(Tag.DESSERT)),
        Recipe(4, "Example Recipe 4", "Description of Example Recipe 4", 20),
        Recipe(5, "Example Recipe 5", "Description of Example Recipe 5", 15),
    )
    RecipeFinderTheme {
        SearchScreen(example)
    }

}