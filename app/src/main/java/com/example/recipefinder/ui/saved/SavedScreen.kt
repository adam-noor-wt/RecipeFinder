package com.example.recipefinder.ui.saved

import com.example.recipefinder.ui.components.LoadingOrEmptyState
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.recipefinder.ui.theme.Secondary

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
private fun AllRecipesTab(
    recipes: List<Recipe>,
    isLoaded: Boolean,
    onRecipeClick: (Recipe) -> Unit
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
        if (!isLoaded || recipes.isEmpty()) {
            LoadingOrEmptyState(isLoaded, "No saved recipes found")
        } else {
            SavedRecipeList(filteredRecipes, onRecipeClick)
        }
    }

}


@Composable
fun SavedRecipeList(
    filteredRecipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit
) {
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
        SavedScreen(example, onRecipeClick = {})
    }

}

@Composable
fun SavedScreen(
    recipes: List<Recipe> = emptyList(),
    isLoaded: Boolean = true,
    folders: Map<String, List<Int>> = emptyMap(),
    onRecipeClick: (Recipe) -> Unit = {},
    onCreateFolder: (String) -> Unit = {},
    onDeleteFolder: (String) -> Unit = {},
    onFolderRecipesChanged: (String, List<Int>) -> Unit = { _, _ -> }
) {
    var selectedTab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var openFolder by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    // If the open folder was deleted, go back to the folder list
    if (openFolder != null && openFolder !in folders) openFolder = null

    androidx.compose.foundation.layout.Column {
        androidx.compose.material3.TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Primary,
            contentColor = Color.White,
            indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Secondary
                    )

            }
        ) {

            listOf("All Recipes", "Folders").forEachIndexed { index, title ->
                androidx.compose.material3.Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index; openFolder = null },
                    text = { Text(title) }
                )
            }
        }
        val current = openFolder
        when {
            selectedTab == 0 -> AllRecipesTab(recipes, isLoaded, onRecipeClick)
            current == null -> FolderListTab(
                folders = folders,
                savedIds = recipes.map { it.id }.toSet(),
                onOpen = { openFolder = it },
                onCreateFolder = onCreateFolder
            )
            else -> FolderDetail(
                name = current,
                allRecipes = recipes,
                recipeIds = folders[current] ?: emptyList(),
                onBack = { openFolder = null },
                onRecipeClick = onRecipeClick,
                onDelete = { onDeleteFolder(current) },
                onChanged = { onFolderRecipesChanged(current, it) }
            )
        }
    }
}

@Composable
private fun FolderListTab(
    folders: Map<String, List<Int>>,
    savedIds: Set<Int>,
    onOpen: (String) -> Unit,
    onCreateFolder: (String) -> Unit
) {
    var showCreate by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {

        if (folders.isEmpty()) {
            Text("No folders yet. Create one (e.g. Dinner, Lunch, Sweets, Favorites).", color = Color.Gray)
        }
        LazyColumn {
            itemsIndexed(folders.keys.toList()) { _, name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(Primary, shape = MaterialTheme.shapes.medium)
                        .clickable { onOpen(name) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${folders[name]?.count { it in savedIds } ?: 0} recipes",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.Button(
            onClick = { showCreate = true },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Primary)
        ) { Text("New Folder", color = Color.White) }
    }
    if (showCreate) {
        var newName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
        val trimmed = newName.trim()
        val duplicate = folders.keys.any { it.equals(trimmed, ignoreCase = true) }
        val fieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
            cursorColor = Color.White,
            errorTextColor = Color.White,
            errorBorderColor = Color.White,
            errorLabelColor = Color.White,
            errorSupportingTextColor = Color.White,
            errorCursorColor = Color.White
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCreate = false },
            containerColor = Primary,
            shape = RoundedCornerShape(16.dp),
            title = { Text("New Folder", color = Color.White, style = MaterialTheme.typography.headlineSmall) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Folder name") },
                    singleLine = true,
                    isError = duplicate,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    supportingText = { if (duplicate) Text("A folder with this name already exists") }
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    enabled = trimmed.isNotEmpty() && !duplicate,
                    onClick = { onCreateFolder(trimmed); showCreate = false },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Primary,
                        disabledContainerColor = Color.White.copy(alpha = 0.4f),
                        disabledContentColor = Primary.copy(alpha = 0.6f)
                    )
                ) { Text("Create") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showCreate = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun FolderDetail(
    name: String,
    allRecipes: List<Recipe>,
    recipeIds: List<Int>,
    onBack: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onDelete: () -> Unit,
    onChanged: (List<Int>) -> Unit
) {
    var showPicker by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var confirmDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val folderRecipes = allRecipes.filter { it.id in recipeIds }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            androidx.compose.material3.IconButton(onClick = { showPicker = true }, modifier = Modifier.size(48.dp)) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Edit,
                    contentDescription = "Edit folder",
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            androidx.compose.material3.IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(48.dp)) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                    contentDescription = "Delete folder",
                    tint = Color.Red,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        if (folderRecipes.isEmpty()) {
            Text("This folder is empty. Tap Edit to add recipes.", color = Color.Gray, modifier = Modifier.padding(16.dp))
        }
        SavedRecipeList(folderRecipes, onRecipeClick)
    }
    if (showPicker) {
        val selected = androidx.compose.runtime.remember {
            androidx.compose.runtime.mutableStateListOf<Int>().apply { addAll(recipeIds) }
        }
        FolderRecipePicker(
            name = name,
            allRecipes = allRecipes,
            selected = selected,
            onDismiss = { showPicker = false },
            onSave = { onChanged(selected.toList()); showPicker = false }
        )
    }
    if (confirmDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete folder?") },
            text = { Text("The recipes themselves will stay saved.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FolderRecipePicker(
    name: String,
    allRecipes: List<Recipe>,
    selected: androidx.compose.runtime.snapshots.SnapshotStateList<Int>,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Recipes in $name",
                style = MaterialTheme.typography.headlineMedium,
                color = Primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Tap recipes to add or remove them",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${selected.size} of ${allRecipes.size} selected",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            if (allRecipes.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No saved recipes to add yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    itemsIndexed(allRecipes) { index, recipe ->
                        val isSelected = recipe.id in selected
                        val backgroundColor = if (index % 2 == 0) Primary else Color.White
                        val contentColor = if (index % 2 == 0) Color.White else Primary
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(5.dp, shape = MaterialTheme.shapes.medium, ambientColor = Color.Black, spotColor = Color.Black)
                                .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                                .clickable {
                                    if (isSelected) selected.remove(recipe.id) else selected.add(recipe.id)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = recipe.imageUrl.ifEmpty { com.example.recipefinder.R.drawable.alfredo },
                                contentDescription = "Recipe Image",
                                modifier = Modifier.size(64.dp).padding(top = 2.dp, end = 2.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recipe.title,
                                    color = contentColor,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (recipe.tags.isNotEmpty()) {
                                    Row {
                                        Tag.sortedByPriority(recipe.tags).take(3).forEach { tag ->
                                            Text(
                                                text = tag.displayName,
                                                color = backgroundColor,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier
                                                    .padding(end = 6.dp)
                                                    .background(contentColor.copy(alpha = 0.9f), shape = MaterialTheme.shapes.extraSmall)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = recipe.description,
                                    color = contentColor.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (it) selected.add(recipe.id) else selected.remove(recipe.id)
                                },
                                colors = androidx.compose.material3.CheckboxDefaults.colors(
                                    checkedColor = contentColor,
                                    checkmarkColor = backgroundColor,
                                    uncheckedColor = contentColor
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.material3.Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Gray, contentColor = Color.White
                    )
                ) { Text("Cancel") }
                androidx.compose.material3.Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Secondary, contentColor = Color.White
                    )
                ) { Text("Save") }
            }
        }
    }
}
