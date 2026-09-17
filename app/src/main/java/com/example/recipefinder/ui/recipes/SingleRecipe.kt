package com.example.recipefinder.ui.recipes

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    saveable: Boolean = true
) {
    val favoritedRecipes = recipeViewModel.savedRecipes.collectAsState()
        Box(
            ){
            // Background image with alpha 0.5
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.recipefinder.R.drawable.alfredo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .background(Primary),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alpha = 0.3f
            )
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(.4f)
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

                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.recipefinder.R.drawable.alfredo), // Placeholder for recipe.image
                            contentDescription = "Recipe Image",
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(.45f).padding(top = 24.dp, end = 8.dp),
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
                        recipe.tags.forEachIndexed { index, tag ->
                            Text(
                                text = tag.name.replace('_', ' ').lowercase()
                                    .replaceFirstChar { it.uppercase() },
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
                Column (
                    modifier = Modifier.verticalScroll(rememberScrollState())
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