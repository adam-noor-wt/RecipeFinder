package com.example.recipefinder.ui.discover

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipefinder.R
import com.example.recipefinder.data.Ingredient
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.RecipeViewModel
import com.example.recipefinder.data.Step
import com.example.recipefinder.data.Tag
import com.example.recipefinder.data.fractionString
import com.example.recipefinder.database.updateSavedRecipesInDatabase
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.RecipeFinderTheme
import com.example.recipefinder.ui.theme.Secondary
import com.example.recipefinder.ui.theme.Typography
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartbreakerComponent(
    recipes: List<Recipe>,
    savedRecipes: MutableList<Recipe>,
    currentSaves: List<Recipe>,
    model: RecipeViewModel,
    open: Boolean,
    onClose: () -> kotlin.Unit = {},
    onShowResults: () -> Unit
) {

    // Recipe queue management
    var currentRecipeIndex by remember(open) { mutableIntStateOf(0) }
    val availableRecipes = remember<List<Recipe>>(recipes, currentSaves) {
        recipes.shuffled().filter{recipe -> !currentSaves.any{it.id == recipe.id}}
    }

    // Create a stable reference to get recipe at a given index
    val getRecipeAtIndex: (Int) -> Recipe? = { index ->
        if (index < availableRecipes.size && availableRecipes.isNotEmpty()) {
            availableRecipes[index]
        } else null
    }

    val currentRecipe = getRecipeAtIndex(currentRecipeIndex)?.also {
        println("HeartbreakerComponent: currentRecipe at index $currentRecipeIndex is ${it.id}:${it.title}")
    }

    // Swipe gesture state
    var offsetX by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX = remember { Animatable(0f) }
    val animatedRotation = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStartTime by remember { mutableLongStateOf(0L) }

    // Bottom sheet state
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showBottomSheet by remember { mutableStateOf(false) }
    var isCardSwipeEnabled by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    // Constants
    val SWIPE_THRESHOLD = 100f

    // Derived state
    val swipeProgress = (abs(offsetX) / SWIPE_THRESHOLD).coerceIn(0f, 1f)
    val isSwipingLeft = offsetX < -20f
    val isSwipingRight = offsetX > 20f
    val shouldCommitSwipe = abs(offsetX) > SWIPE_THRESHOLD

    // Monitor bottom sheet state to disable card swipe when expanded
    LaunchedEffect(bottomSheetState.currentValue) {
        isCardSwipeEnabled = bottomSheetState.currentValue != SheetValue.Expanded
    }

    // Functions
    fun animateCardExit(direction: Float) {
        coroutineScope.launch {
            animatedOffsetX.animateTo(
                targetValue = direction * 1000f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
        coroutineScope.launch {
            animatedRotation.animateTo(
                targetValue = if (direction > 0) 15f else -15f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
    }

    fun resetCardPosition() {
        offsetX = 0f
        rotation = 0f
        coroutineScope.launch {
            animatedOffsetX.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
        }
        coroutineScope.launch {
            animatedRotation.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
        }
    }

    fun moveToNextRecipe() {
        println("HeartbreakerComponent: moveToNextRecipe called. Index $currentRecipeIndex -> ${currentRecipeIndex + 1}")
        currentRecipeIndex++
        offsetX = 0f
        rotation = 0f
        isDragging = false
        showBottomSheet = false
    }

    suspend fun handleSwipeCommit(recipeToSave: Recipe?) {
        val direction = if (offsetX > 0) 1f else -1f
        println("HeartbreakerComponent: handleSwipeCommit - received recipe: ${recipeToSave?.id}:${recipeToSave?.title}")

        // Animate card exit
        animateCardExit(direction)

        // Wait for animation
        delay(300)

        // Move to next recipe
        moveToNextRecipe()

        // Reset animations
        animatedOffsetX.snapTo(0f)
        animatedRotation.snapTo(0f)

        // Perform action AFTER moving to next (using captured recipe)
        if (direction > 0) {
            // Save recipe (avoid duplicates)
            recipeToSave?.let { recipe ->
                println("HeartbreakerComponent: Attempting to save recipe ${recipe.id} '${recipe.title}'")
                println("HeartbreakerComponent: Current list has ${savedRecipes.size} recipes: ${savedRecipes.map { "${it.id}:${it.title}" }}")
                val alreadyExists = savedRecipes.any { it.id == recipe.id }
                println("HeartbreakerComponent: Already exists? $alreadyExists")
                if (!alreadyExists) {
                    savedRecipes.add(recipe)
                    println("HeartbreakerComponent: Added! List now has ${savedRecipes.size} recipes")
                }
            }
        }
        // For left swipe (discard), no action needed
    }

    if (open) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(color = Primary.copy(alpha = 0.8f))
                .clickable { onClose() },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Main content area - prevent click-through to background
                Box(
                    modifier = Modifier.clickable(enabled = false) { },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentRecipe == null) {
                        // Empty state
                        EmptyStateCard()
                    } else {
                        // Swipeable card stack
                        Box(
                            modifier = Modifier
                                .width(350.dp)
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Action indicators (behind card)
                            if (isSwipingLeft) {
                                DiscardIndicator(swipeProgress)
                            }
                            if (isSwipingRight) {
                                SaveIndicator(swipeProgress)
                            }

                            // The recipe card
                            RecipeCard(
                                recipe = currentRecipe,
                                offsetX = offsetX + animatedOffsetX.value,
                                rotation = rotation + animatedRotation.value,
                                isCardSwipeEnabled = isCardSwipeEnabled,
                                isDragging = isDragging,
                                onDragStart = {
                                    isDragging = true
                                    dragStartTime = System.currentTimeMillis()
                                },
                                onDrag = { dragAmount ->
                                    offsetX += dragAmount
                                    rotation = (offsetX / 20f).coerceIn(-15f, 15f)
                                },
                                onDragEnd = {
                                    // Capture the INDEX and look up recipe from stable list
                                    val indexAtSwipeTime = currentRecipeIndex
                                    val recipeAtSwipeTime = getRecipeAtIndex(indexAtSwipeTime)
                                    println("HeartbreakerComponent: onDragEnd - captured index $indexAtSwipeTime, recipe ${recipeAtSwipeTime?.id}:${recipeAtSwipeTime?.title}")
                                    coroutineScope.launch {
                                        val dragDuration =
                                            System.currentTimeMillis() - dragStartTime
                                        val dragDistance = abs(offsetX)

                                        // Distinguish tap from swipe
                                        if (dragDuration < 100 && dragDistance < 10f) {
                                            showBottomSheet = true
                                            resetCardPosition()
                                        } else if (dragDistance > SWIPE_THRESHOLD) {
                                            handleSwipeCommit(recipeAtSwipeTime)
                                        } else {
                                            resetCardPosition()
                                        }
                                    }
                                    isDragging = false
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        resetCardPosition()
                                    }
                                    isDragging = false
                                },
                                onTap = {
                                    if (!isDragging && isCardSwipeEnabled) {
                                        showBottomSheet = true
                                    }
                                }
                            )
                        }
                    }
                }

                // Bottom sheet for recipe details
                if (showBottomSheet && currentRecipe != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
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
                        RecipeDetailsContent(recipe = currentRecipe)
                    }
                }
                Button(
                    colors = ButtonColors(Secondary, Color.White, Color.Gray, Color.Gray),
                    onClick = {
                    onShowResults()
                    onClose()
                }) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    offsetX: Float,
    rotation: Float,
    isCardSwipeEnabled: Boolean,
    isDragging: Boolean,
    onDragStart: () -> kotlin.Unit,
    onDrag: (Float) -> kotlin.Unit,
    onDragEnd: () -> kotlin.Unit,
    onDragCancel: () -> kotlin.Unit,
    onTap: () -> kotlin.Unit
) {
    Card(
        modifier = Modifier
            .width(350.dp)
            .height(400.dp)
            .graphicsLayer {
                translationX = offsetX
                rotationZ = rotation
            }
            .pointerInput(isCardSwipeEnabled) {
                if (isCardSwipeEnabled) {
                    detectHorizontalDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragCancel() },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount)
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            AsyncImage(
                model = recipe.imageUrl.ifEmpty { R.drawable.alfredo },
                contentDescription = "Recipe image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )

            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section: Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tag.sortedByPriority(recipe.tags)
                        .take(4)
                        .forEach { tag ->
                            Text(
                                text = tag.displayName,
                                color = Primary,
                                style = Typography.labelSmall,
                                modifier = Modifier
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                }

                // Bottom section: Recipe info
                Column {
                    Text(
                        text = recipe.title,
                        color = Color.White,
                        style = Typography.headlineMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${recipe.preparationTime} min",
                            color = Color.White,
                            style = Typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = recipe.description,
                        color = Color.White.copy(alpha = 0.9f),
                        style = Typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun DiscardIndicator(swipeProgress: Float) {
    AnimatedVisibility(visible = true) {
        Box(
            modifier = Modifier
                .width(350.dp)
                .height(400.dp)
                .background(
                    color = Color.Red.copy(alpha = swipeProgress * 0.7f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Discard",
                tint = Color.White,
                modifier = Modifier
                    .size((40.dp.value + (swipeProgress * 20f)).coerceAtMost(60f).dp)
                    .graphicsLayer {
                        alpha = swipeProgress
                        scaleX = 0.8f + (swipeProgress * 0.2f)
                        scaleY = 0.8f + (swipeProgress * 0.2f)
                    }
            )
        }
    }
}

@Composable
fun SaveIndicator(swipeProgress: Float) {
    AnimatedVisibility(visible = true) {
        Box(
            modifier = Modifier
                .width(350.dp)
                .height(400.dp)
                .background(
                    color = Color(0xFFFFD700).copy(alpha = swipeProgress * 0.7f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Save",
                tint = Color.White,
                modifier = Modifier
                    .size((40.dp.value + (swipeProgress * 20f)).coerceAtMost(60f).dp)
                    .graphicsLayer {
                        alpha = swipeProgress
                        scaleX = 0.8f + (swipeProgress * 0.2f)
                        scaleY = 0.8f + (swipeProgress * 0.2f)
                    }
            )
        }
    }
}

@Composable
fun EmptyStateCard() {
    Box(
        modifier = Modifier
            .width(350.dp)
            .height(400.dp)
            .background(
                color = Primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "No more recipes!",
                style = Typography.headlineMedium,
                color = Primary
            )
            Text(
                text = "You've seen all available recipes.",
                style = Typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecipeDetailsContent(recipe: Recipe) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Text(
            text = recipe.title,
            style = Typography.headlineMedium,
            color = Primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Ingredients Section
        Text(
            text = "Ingredients",
            style = Typography.titleLarge,
            color = Secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier
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
                .padding(8.dp)
        ) {
            recipe.ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "•",
                        color = Secondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "${ingredient.quantity.fractionString()} ${ingredient.unit.toString().lowercase()} ",
                        style = Typography.bodyLarge
                    )
                    Text(
                        text = "${ingredient.name} ",
                        fontWeight = FontWeight.Bold,
                        style = Typography.bodyLarge
                    )
                    if (ingredient.note.isNotEmpty()) {
                        Text(
                            text = ingredient.note,
                            color = Color.Gray,
                            style = Typography.bodyLarge.copy(fontStyle = FontStyle.Italic)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Steps Section
        Text(
            text = "Instructions",
            style = Typography.titleLarge,
            color = Secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        recipe.steps.forEach { step ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Step ${step.index}: ${step.title}",
                    style = Typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = step.description,
                    style = Typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@SuppressLint("ViewModelConstructorInComposable", "UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun HeartbreakerComponentPreview() {
    val example: MutableList<Recipe> = mutableListOf(
        Recipe(
            1,
            "Example Recipe 1",
            "Description of Example Recipe 1",
            40,
            tags = listOf(Tag.SOUP, Tag.LUNCH, Tag.PASTA, Tag.DAIRY_FREE, Tag.TOFU, Tag.GLUTEN_FREE),
            ingredients = listOf(
                Ingredient(1, "Pasta", 2.0, com.example.recipefinder.data.Unit.CUP, ""),
                Ingredient(2, "Tomato Sauce", 1.0, com.example.recipefinder.data.Unit.CUP, "")
            ),
            steps = listOf(
                Step(1, "Boil water", "Bring a large pot of water to boil"),
                Step(2, "Cook pasta", "Add pasta and cook for 10 minutes")
            )
        ),
        Recipe(2, "Example Recipe 2", "Description of Example Recipe 2", 80, tags = listOf(Tag.BREAKFAST, Tag.VEGAN)),
        Recipe(3, "Example Recipe 3", "Description of Example Recipe 3", 120, tags = listOf(Tag.DESSERT)),
    )
    RecipeFinderTheme {
        HeartbreakerComponent(
            recipes = example,
            savedRecipes = mutableListOf(),
            model = RecipeViewModel(),
            currentSaves = listOf(),
            open = true,
            onShowResults = {  }
        )
    }
}
