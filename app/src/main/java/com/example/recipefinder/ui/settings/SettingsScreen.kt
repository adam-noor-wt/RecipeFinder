package com.example.recipefinder.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.Tag
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.RecipeFinderTheme
import com.example.recipefinder.ui.theme.Secondary
import com.example.recipefinder.ui.theme.Tertiary
import com.example.recipefinder.ui.theme.Typography

@Composable
private fun AccountCard(
    id: Int,
    selector: Int,
    text: String,
    mainText: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)?
)
{
    var useColor = accentColor
    var backgroundColor = Color.White
    if(selector == id)
    {
        useColor = Color.White
        backgroundColor = Secondary
    }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon with circular background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        useColor.copy(alpha = 0.15f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "$text: $mainText",
                    tint = useColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Number
            Text(
                text = mainText,
                style = Typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = useColor
                ),
                modifier = Modifier.padding(top = 5.dp)
            )

            // Label
            Text(
                text = text,
                style = Typography.labelMedium.copy(
                    color = if (selector != id) Color.Gray.copy(alpha = 0.7f) else Color.White
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MyAccount(
    displayName: String?,
    onEditDisplayName: (String) -> Unit,
    edit: MutableState<Boolean>
)
{
    val focusRequester = remember { FocusRequester() }

    // Request focus when edit mode becomes true
    androidx.compose.runtime.LaunchedEffect(edit.value) {
        if (edit.value) {
            focusRequester.requestFocus()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Account Logo",
            tint = Primary,
            modifier = Modifier.size(100.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(!edit.value) {
                Text(
                    displayName ?: "Not set",
                    style = Typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(.8f),
                    textAlign = TextAlign.Right,
                    softWrap = true,
                    color = Secondary
                )
                FilledIconButton(
                    onClick = {
                        edit.value = true
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Primary,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.DarkGray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Display Name",
                        tint = Primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            else
            {
                val userName = remember{mutableStateOf(displayName ?: "Anonymous")}
                val maxChar = 10 //Text length limit
                val interactionSource = remember { MutableInteractionSource() }
                val hasFocused = remember { mutableStateOf(false) }
                BasicTextField(
                    value = userName.value,
                    onValueChange = {
                        if (it.length <= maxChar) {
                            userName.value = it
                        }
                    },
                    interactionSource = interactionSource,
                    enabled = edit.value,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if(userName.value.length in 1..<10) {
                                onEditDisplayName(userName.value)
                            }
                            edit.value = false
                            hasFocused.value = false
                        }
                    ),
                    modifier = Modifier
                        .widthIn(min = 50.dp)
                        .width(IntrinsicSize.Min)
                        .padding(end = 5.dp)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                hasFocused.value = true
                            } else if (hasFocused.value) {
                                // Only close if we've previously had focus
                                edit.value = false
                                hasFocused.value = false
                            }
                        }
                        .focusRequester(focusRequester),
                    textStyle = Typography.headlineMedium.copy(color = Secondary),

                ) { innerTextField ->
                    Column {
                        innerTextField()
                        Text(
                            text = "${userName.value.length} / $maxChar",
                            style = Typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    }
                }
                FilledIconButton(
                    onClick = {
                        if(userName.value.length in 1..<10) {
                            onEditDisplayName(userName.value)
                        }
                        edit.value = false
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Primary,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.DarkGray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Confirm Edit Display Name",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MyRecipeItem(
    recipe: Recipe,
    onRecipeClick: (Recipe) -> Unit?,
    backgroundColor: Color,
    contentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                5.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = androidx.compose.ui.graphics.Color.Black,
                spotColor = androidx.compose.ui.graphics.Color.Black
            )
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .clickable { onRecipeClick(recipe) }
            .padding(8.dp),
    ) {
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

@Composable
fun SettingsScreen(
    displayName: String?,
    onEditDisplayName: (String) -> Unit,
    signOut: () -> Unit,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit?,
    savedRecipes: List<Recipe>
) {
    val edittingName = remember{mutableStateOf(false)}
    val selector = remember{mutableIntStateOf(1)}
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyAccount(displayName, onEditDisplayName, edittingName)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.18f)
                .padding(horizontal = 8.dp)
        )
        {
            AccountCard(
                id = 1,
                selector = selector.intValue,
                text = "Recipes Created",
                mainText = recipes.size.toString(),
                icon = Icons.Filled.Create,
                accentColor = Primary,
                modifier = Modifier.weight(1f),
                onClick = { selector.intValue = 1 }

            )
            AccountCard(
                id = 2,
                selector = selector.intValue,
                text = "Recipes Saved",
                mainText = savedRecipes.size.toString(),
                icon = Icons.Filled.Bookmark,
                accentColor = Primary,
                modifier = Modifier.weight(1f),
                onClick = { selector.intValue = 2 }
            )
            AccountCard(
                id = 3,
                selector = selector.intValue,
                text = "Recipes Made",
                mainText = "0",
                icon = Icons.Filled.Restaurant,
                accentColor = Primary,
                modifier = Modifier.weight(1f),
                onClick = { selector.intValue = 3 }
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        when (selector.intValue) {
            1 -> Text(text = "Recipes You Created", style = Typography.titleLarge, color = Primary)
            2 -> Text(text = "Recipes You Saved", style = Typography.titleLarge, color = Primary)
            3 -> Text(text = "Coming soon..", style = Typography.titleLarge, color = Primary)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.8f)
                .padding(8.dp)
                .background(androidx.compose.ui.graphics.Color.Transparent)
        ) {
            when (selector.intValue) {
                1 -> {
                    itemsIndexed(recipes) { index, recipe ->
                        val backgroundColor =
                            if (index % 2 == 0) Primary else androidx.compose.ui.graphics.Color.White
                        val contentColor =
                            if (index % 2 == 0) androidx.compose.ui.graphics.Color.White else Primary

                        MyRecipeItem(recipe, onRecipeClick, backgroundColor, contentColor)
                    }
                }
                2 -> {
                    itemsIndexed(savedRecipes) { index, recipe ->
                        val backgroundColor =
                            if (index % 2 == 0) Primary else androidx.compose.ui.graphics.Color.White
                        val contentColor =
                            if (index % 2 == 0) androidx.compose.ui.graphics.Color.White else Primary

                        MyRecipeItem(recipe, onRecipeClick, backgroundColor, contentColor)
                    }
                }
                else -> {
                    item { Text("Coming soon...") }
                }
            }
        }

        OutlinedButton(
            onClick = signOut,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
        ){
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Sign Out Icon",
                tint = Color.Red,
                modifier = Modifier.size(30.dp)
            )
            Text("Sign Out", style=Typography.bodyLarge)
        }

    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val example: MutableList<Recipe>  = mutableListOf(
        Recipe(1, "Example Recipe 1",  "Description of Example Recipe 1", 40, tags = listOf(Tag.SOUP, Tag.LUNCH, Tag.PASTA, Tag.DAIRY_FREE, Tag.TOFU, Tag.GLUTEN_FREE)),
        Recipe(2, "Example Recipe 2", "Description of Example Recipe 2", 80, tags = listOf(Tag.BREAKFAST, Tag.VEGAN)),
        Recipe(3, "Example Recipe 3", "Description of Example Recipe 3", 120, tags = listOf(Tag.DESSERT)),
        Recipe(4, "Example Recipe 4", "Description of Example Recipe 4", 20),
        Recipe(5, "Example Recipe 5", "Description of Example Recipe 5", 15),
    )
    RecipeFinderTheme {
        SettingsScreen(
            displayName = "Anonymous",
            onEditDisplayName = {print("a")},
            signOut = { print("A") },
            recipes = example,
            onRecipeClick = {recipe -> print(recipe)},
            savedRecipes = example
        )
    }
}