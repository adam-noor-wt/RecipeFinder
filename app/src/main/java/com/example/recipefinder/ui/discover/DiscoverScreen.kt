package com.example.recipefinder.ui.discover

import android.annotation.SuppressLint
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.RecipeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.recipefinder.SingleRecipe
import com.example.recipefinder.data.Tag
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.RecipeFinderTheme
import com.example.recipefinder.ui.theme.Secondary
import com.example.recipefinder.ui.theme.Typography

@Composable
fun GameButton(
    text: String,
    recipes: List<Recipe>,
    icon: ImageVector,
    onClick: () -> Unit,
    info: String
)
{
    val showInfo = remember { (mutableStateOf(false)) }

    Box(
        modifier = Modifier
            .width(165.dp)
            .height(165.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = com.example.recipefinder.R.drawable.alfredo),
            contentDescription = "Play $text",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(
                color = Secondary.copy(alpha = 0.8f),
                blendMode = BlendMode.Darken
            )
        )

        // Center content area - icon/title or info text
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if(showInfo.value){
                Text(
                    text = info,
                    color = Color.White,
                    style = Typography.bodyMedium.copy(fontSize = 18.sp),
                    textAlign = TextAlign.Center,
                    softWrap = true,
                )
            }
            else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$text Icon",
                        tint = Color.White,
                        modifier = Modifier.size(66.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = text,
                        color = Color.White,
                        style = Typography.headlineMedium.copy(fontSize = 22.sp)
                    )
                }
            }
        }

        // Info/Close button (always on top)
        IconButton(
            onClick = { showInfo.value = !showInfo.value },
            modifier = Modifier
                .align(alignment = Alignment.TopEnd)
                .padding(6.dp)
                .size(35.dp)
        )
        {
            Icon(
                imageVector = if (showInfo.value) Icons.Outlined.Close else Icons.Outlined.Info,
                contentDescription = if (showInfo.value) "$text Close Info" else "$text More Info",
                tint = Color.White,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}

@Composable
fun DiscoverScreen(
    recipes: List<Recipe>,
    model: RecipeViewModel,
    onRecipeClick: (Recipe) -> Unit = {},
    navigator: NavHostController?,
    currentSaves: List<Recipe>
)
{
    var showHeartbreaker by remember { mutableStateOf(false) }
    var showHeartbreakerResults by remember { mutableStateOf(false) }
    val heartbreakerSavedRecipes = remember { mutableStateListOf<Recipe>() }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    )
    {
        Box(
            modifier = Modifier
                .fillMaxHeight(.1f)
                .fillMaxWidth()
                .padding(bottom = 4.dp)
                .background(Primary)
        )
        {
            Column() {
                Text(
                    "Discover",
                    style = Typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top=5.dp)
                )
                Text(
                    "Find your perfect recipe in a unique and fun way",
                    style = Typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            GameButton("Heartbreaker", recipes, Icons.Filled.HeartBroken, { showHeartbreaker = true }, info = "Swipe left if you're not interested, and swipe right if you are. You can always tap to view more too!")
            GameButton("Random", recipes, Icons.Filled.QuestionMark, { navigator?.navigate("${SingleRecipe.route}/${recipes.random().id}") }, info = "Find something new! This will take you to an entirely random recipe!")
        }
    }

    // Heartbreaker modal
    HeartbreakerComponent(
        recipes = recipes,
        savedRecipes = heartbreakerSavedRecipes,
        model = model,
        open = showHeartbreaker,
        currentSaves = currentSaves,
        onClose = {
            showHeartbreaker = false
        },
        onShowResults = {
            showHeartbreaker = false
            showHeartbreakerResults = true
        }
    )

    // Results modal
    HeartbreakerResultsModal(
        heartbreakerRecipes = heartbreakerSavedRecipes,
        currentlySaved = currentSaves,
        model = model,
        open = showHeartbreakerResults,
        onClose = {
            showHeartbreakerResults = false
            // Clear the list after viewing results so next game starts fresh
            heartbreakerSavedRecipes.clear()
        }
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun DiscoverScreenPreview() {
    val example: MutableList<Recipe>  = mutableListOf(
        Recipe(1, "Example Recipe 1",  "Description of Example Recipe 1", 40, tags = listOf(Tag.SOUP, Tag.LUNCH, Tag.PASTA, Tag.DAIRY_FREE, Tag.TOFU, Tag.GLUTEN_FREE)),
        Recipe(2, "Example Recipe 2", "Description of Example Recipe 2", 80, tags = listOf(Tag.BREAKFAST, Tag.VEGAN)),
        Recipe(3, "Example Recipe 3", "Description of Example Recipe 3", 120, tags = listOf(Tag.DESSERT)),
        Recipe(4, "Example Recipe 4", "Description of Example Recipe 4", 20),
        Recipe(5, "Example Recipe 5", "Description of Example Recipe 5", 15),
    )
    RecipeFinderTheme {
        DiscoverScreen(example, RecipeViewModel(), {}, navigator = null, currentSaves = listOf())
    }

}