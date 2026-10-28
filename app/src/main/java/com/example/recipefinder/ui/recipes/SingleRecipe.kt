package com.example.recipefinder.ui.recipes

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.recipefinder.data.Ingredient
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.RecipeViewModel
import com.example.recipefinder.data.Step
import com.example.recipefinder.data.Tag
import com.example.recipefinder.data.Unit
import com.example.recipefinder.data.fractionString
import com.example.recipefinder.database.updateSavedRecipesInDatabase
import com.example.recipefinder.recipeViewModel
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.RecipeFinderTheme
import com.example.recipefinder.ui.theme.Secondary
import com.example.recipefinder.ui.theme.Typography
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun SingleRecipeScreen(
    recipe: Recipe,
    model: RecipeViewModel,
    saveable: Boolean = true,
    onEditClick: (Recipe) -> kotlin.Unit = {}
) {
    val favoritedRecipes = recipeViewModel.savedRecipes.collectAsState()

    // Scroll state for unified container
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Constants
    val COLLAPSE_THRESHOLD = 400f

    // Derived collapse progress (0f = expanded, 1f = collapsed)
    val collapseProgress by remember {
        derivedStateOf {
            (scrollState.value.toFloat() / COLLAPSE_THRESHOLD).coerceIn(0f, 1f)
        }
    }

    // Boolean for fully collapsed state
    val isCollapsed by remember {
        derivedStateOf {
            scrollState.value >= COLLAPSE_THRESHOLD.toInt()
        }
    }

        Box(
            modifier = Modifier.fillMaxSize()
            ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            // Slide header up as user scrolls
                            translationY = -(scrollState.value.toFloat().coerceAtMost(COLLAPSE_THRESHOLD))
                            // Fade out header content
                            alpha = 1f - collapseProgress
                        }
                ) {
                // Background image with alpha that fades during collapse
                AsyncImage(
                    model = recipe.imageUrl.ifEmpty { com.example.recipefinder.R.drawable.alfredo },
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Primary),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    alpha = 0.3f
                )
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val recipeTitle: String = recipe.title
                    Row {
                        Column(
                            modifier = Modifier.fillMaxWidth(.60f)
                                .fillMaxHeight(.55f)
                        ) {
                            val showPopup = remember { mutableStateOf(false) }

                            Box {
                                Text(
                                    text = recipeTitle,
                                    modifier = Modifier
                                        .padding(16.dp, top = 16.dp)
                                        .clickable { showPopup.value = true },
                                    color = Color.White,
                                    style = Typography.headlineMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (showPopup.value) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { showPopup.value = false },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = recipeTitle,
                                                color = Primary,
                                                style = Typography.titleMedium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "Cook Time: ${recipe.preparationTime} minutes",
                                modifier = Modifier.padding(start = 16.dp),
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Row() {
                                    if (saveable) {
                                        if (favoritedRecipes.value.contains(recipe)) {
                                            Button(
                                                onClick = {
                                                    model.setSavedRecipes(favoritedRecipes.value.filter { it.id != recipe.id })
                                                    updateSavedRecipesInDatabase(
                                                        Firebase.firestore,
                                                        Firebase.auth.currentUser?.uid ?: "",
                                                        favoritedRecipes.value.filter { it.id != recipe.id })
                                                },
                                                colors = ButtonColors(
                                                    containerColor = Color(0xFFFFD700),
                                                    contentColor = Color(0xFFFFD700),
                                                    disabledContentColor = Color.Gray,
                                                    disabledContainerColor = Color.DarkGray
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.width(100.dp)
                                            ) {
                                                Text(
                                                    text = "Saved",
                                                    color = Color.White,
                                                    style = Typography.bodyLarge.copy(letterSpacing = 2.sp)
                                                )
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    model.setSavedRecipes(favoritedRecipes.value + recipe)
                                                    updateSavedRecipesInDatabase(
                                                        Firebase.firestore,
                                                        Firebase.auth.currentUser?.uid ?: "",
                                                        favoritedRecipes.value + recipe
                                                    )
                                                },
                                                colors = ButtonColors(
                                                    containerColor = Color.White,
                                                    contentColor = Color.White,
                                                    disabledContentColor = Color.Gray,
                                                    disabledContainerColor = Color.DarkGray
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.width(100.dp)
                                            ) {
                                                Text(
                                                    text = "Save",
                                                    color = Color(0xFFFFD700),
                                                    style = Typography.bodyLarge.copy(letterSpacing = 2.sp)
                                                )
                                            }
                                        }
                                    }
                                    if (recipe.owner != "Anonymous" && recipe.owner ==  Firebase.auth.currentUser?.uid)
                                    {
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Button(
                                            onClick = {
                                                onEditClick(recipe)
                                            },
                                            colors = ButtonColors(
                                                containerColor = Secondary,
                                                contentColor = Color.White,
                                                disabledContentColor = Color.Gray,
                                                disabledContainerColor = Color.DarkGray
                                            ),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.width(100.dp)
                                        ) {
                                            Text(
                                                text = "Edit",
                                                color = Color.White,
                                                style = Typography.bodyLarge.copy(letterSpacing = 2.sp)
                                            )
                                        }
                                    }
                                }
                            }



                        }

                        AsyncImage(
                            model = recipe.imageUrl.ifEmpty { com.example.recipefinder.R.drawable.alfredo },
                            contentDescription = "Recipe Image",
                            modifier = Modifier.size(150.dp).padding(top = 14.dp, end = 8.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Spacer(
                            modifier = Modifier.fillMaxWidth(.1f)
                        )
                    }
                    Text(
                        text = recipe.description,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp).fillMaxWidth(),
                        color = Color.White,
                        style = Typography.bodyLarge,
                    )
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                    ) {
                        Tag.sortedByPriority(recipe.tags).forEachIndexed { index, tag ->
                            Text(
                                text = tag.displayName,
                                color = Primary,
                                style = Typography.labelMedium,
                                modifier = Modifier
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(
                                            8.dp
                                        )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            if (index != recipe.tags.lastIndex) {
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                        }
                    }
                }
                }
                Column (
                    modifier = Modifier
                ) {
                    // Ingredients
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .background(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Secondary,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        recipe.ingredients.forEach { ingredient ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                            ) {
                                Text(
                                    text = "\u2022", // Bullet point
                                    color = Secondary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Text(
                                    text = "${ingredient.quantity.fractionString()} ${ingredient.unit.toString().lowercase()} ",
                                    color = Color.Black,
                                    style = Typography.bodyLarge
                                )
                                Text(
                                    text = "${ingredient.name} ",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    style = Typography.bodyLarge
                                )
                                Text(
                                    text = ingredient.note,
                                    color = Color.Gray,
                                    style = Typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                                )

                            }
                        }
                    }
                    // Steps
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .background(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )

                    ) {
                        recipe.steps.forEach { step ->
                            Column {
                                Text(
                                    text = "Step ${step.index}: ${step.title}",
                                    modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 4.dp).fillMaxWidth(),
                                    style = Typography.titleLarge
                                )
                                Text(
                                    text = step.description,
                                    modifier = Modifier.padding(8.dp, 4.dp, 8.dp, 8.dp).fillMaxWidth(),
                                    style = Typography.bodyLarge
                                )
                            }

                        }
                    }
                }

            }

            // Overlay collapsed bar at top
            CollapsedHeaderBar(
                recipe = recipe,
                isVisible = isCollapsed,
                onExpand = {
                    val job = coroutineScope.launch {
                        scrollState.animateScrollTo(
                            value = 0,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

}

@Composable
private fun CollapsedHeaderBar(
    recipe: Recipe,
    isVisible: Boolean,
    onExpand: () -> kotlin.Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)) + expandVertically(),
        exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { onExpand() },
            color = Primary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.title,
                    color = Color.White,
                    style = Typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand header",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun SingleRecipeScreenPreview() {
    val example = Recipe(1, "Example Recipe 1", "Description of Example Recipe 1", 40, tags = listOf(Tag.SOUP, Tag.LUNCH, Tag.PASTA, Tag.DAIRY_FREE, Tag.TOFU, Tag.GLUTEN_FREE), ingredients = listOf(
        Ingredient(1, "Ingredient 1", 2.0, Unit.CUP, note = "Example note for ingredient 1"),
        Ingredient(2, "Ingredient 2", 1.0, Unit.TABLESPOON),
        Ingredient(3, "Ingredient 3", 3.0, Unit.TEASPOON)
    ), steps = listOf(
        Step(1, "This would be step 1", "This is the first step of the recipe. This is long winded to show that it can handle much longer descriptions."),
        Step(2, "This would be step 2", "This is the second step of the recipe. It can also be long. For example, you might need to explain how to prepare the ingredients or how to cook them."),
        Step(3, "Step 3", "This is the third step of the recipe. It can also be long. For example, you might need to explain how to prepare the ingredients or how to cook them.")
    ))
    RecipeFinderTheme {
        SingleRecipeScreen(example, RecipeViewModel())
    }

}