package com.example.recipefinder

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.recipefinder.data.Ingredient
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.RecipeViewModel
import com.example.recipefinder.data.Step
import com.example.recipefinder.data.Tag
import com.example.recipefinder.data.Unit
import com.example.recipefinder.database.readRecipesFromDatabase
import androidx.compose.runtime.collectAsState
import com.example.recipefinder.database.readSavedRecipesFromDatabase
import com.example.recipefinder.database.writeRecipeToDatabase
import com.example.recipefinder.ui.components.RecipeFinderTabRow
import com.example.recipefinder.ui.createrecipe.CreateRecipeScreen
import com.example.recipefinder.ui.home.HomeScreen
import com.example.recipefinder.ui.login.AccountServiceImpl
import com.example.recipefinder.ui.discover.DiscoverScreen
import com.example.recipefinder.ui.recipes.SingleRecipeScreen
import com.example.recipefinder.ui.saved.SavedScreen
import com.example.recipefinder.ui.search.SearchScreen
import com.example.recipefinder.ui.settings.SettingsScreen
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.RecipeFinderTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.recipefinder.database.updateRecipeInDatabase
import com.example.recipefinder.ui.editrecipe.EditRecipeScreen


fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        //hide the navigation bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        auth = Firebase.auth
        val db = Firebase.firestore
        enableEdgeToEdge()
        setContent {
            RecipeFinderApp(db, auth)
        }
    }


}

val tomato = Ingredient(id = 1, name = "Tomato", quantity = 1.0, unit = Unit.CUP)
val tofu = Ingredient(id = 2, name = "Tofu", quantity = 200.0, unit = Unit.GRAM)
val tomatoSauce = Ingredient(id = 3, name = "Tomato Sauce", quantity = 1.0, unit = Unit.CUP)
val pasta = Ingredient(id = 4, name = "Pasta", quantity = 200.0, unit = Unit.GRAM)
val salt = Ingredient(id = 5, name = "Salt", quantity = 1.0, unit = Unit.TEASPOON)
val pepper = Ingredient(id = 6, name = "Pepper", quantity = 1.0, unit = Unit.TEASPOON)

val example: MutableList<Recipe>  = mutableListOf(
    Recipe(1, "Example Recipe 1", "Description of Example Recipe 1", 80, tags = listOf(Tag.SOUP, Tag.LUNCH, Tag.PASTA, Tag.DAIRY_FREE, Tag.TOFU, Tag.GLUTEN_FREE), ingredients = listOf(tofu, tomato, pasta)),
    Recipe(2, "Example Recipe 2", "Description of Example Recipe 2", 40, tags = listOf(Tag.BREAKFAST, Tag.VEGAN), ingredients = listOf(tomatoSauce, pepper)),
    Recipe(3, "Example Recipe 3", "Description of Example Recipe 3", 120, tags = listOf(Tag.DESSERT), ingredients = listOf(salt, pepper)),
    Recipe(4, "Example Recipe 4", "Description of Example Recipe 4", 60),
    Recipe(5, "Example Recipe 5", "Description of Example Recipe 5", 40, tags = listOf(Tag.SOUP, Tag.LUNCH, Tag.PASTA, Tag.DAIRY_FREE, Tag.TOFU, Tag.GLUTEN_FREE), ingredients = listOf(
        Ingredient(1, "Ingredient 1", 2.0, Unit.CUP, note = "Example note for ingredient 1"),
        Ingredient(2, "Ingredient 2", 1.0, Unit.TABLESPOON),
        Ingredient(3, "Ingredient 3", 3.0, Unit.TEASPOON)
    ), steps = listOf(
        Step(0, "This would be step 1", "This is the first step of the recipe. This is long winded to show that it can handle much longer descriptions."),
        Step(1, "This would be step 2", "This is the second step of the recipe. It can also be long. For example, you might need to explain how to prepare the ingredients or how to cook them."),
        Step(2, "Step 3", "This is the third step of the recipe. It can also be long. For example, you might need to explain how to prepare the ingredients or how to cook them.")
    )),

)


val recipeViewModel = RecipeViewModel()
private fun getDatabaseRecipes(foundRecipes: List<Recipe>, savedRecipes: List<Recipe>) {
    recipeViewModel.setRecipes(foundRecipes)
    recipeViewModel.setSavedRecipes(savedRecipes)
}

fun fetchRecipes(database: FirebaseFirestore) {
    // Simulate fetching recipes from a database
    readRecipesFromDatabase(db = database,
        { foundRecipes ->
            readSavedRecipesFromDatabase(database, foundRecipes, Firebase.auth.currentUser?.uid.toString()) { savedRecipes ->
                // Update the ViewModel with the fetched recipes
                getDatabaseRecipes(foundRecipes, savedRecipes)
            }
        },
        {
            // Handle error case, e.g., show a message to the user
            println("Error fetching recipes: $it")
        }
    )
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun RecipeFinderApp(database: FirebaseFirestore, auth: FirebaseAuth?) {
    val recipes = recipeViewModel.recipes.collectAsState()
    val savedRecipes = recipeViewModel.savedRecipes.collectAsState()
    RecipeFinderTheme {
        // Get the NavController for navigation and the current backstack entry
        val navController = rememberNavController()
        val currentBackstack = navController.currentBackStackEntryAsState()
        // Fetch current destination
        val currentDestination = currentBackstack.value?.destination
        // Default to SingleRecipe if no current destination is found (making the tab row not highlight any)
        val currentScreen = recipeDestinations.find{it.route == currentDestination?.route} ?: SingleRecipe
        Scaffold(
            bottomBar = {
                if(auth != null && auth.currentUser != null) {
                    RecipeFinderTabRow(
                        allScreens = recipeDestinations,
                        onTabSelected = { destination ->
                            fetchRecipes(database)
                            // Navigate to the selected destination
                            // Pop everything up to start destination to clear EditRecipe, SingleRecipe, CreateRecipe
                            navController.navigate(destination.route) {
                                popUpTo(Home.route) {
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        currentScreen = currentScreen
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),

            floatingActionButton = {
                if(auth != null && auth.currentUser != null)
                {
                    val currentRoute = currentBackstack.value?.destination?.route
                    val shouldShowFab = currentRoute != CreateRecipe.route &&
                                       currentRoute != Settings.route &&
                                       currentRoute != Home.route &&
                                       currentRoute != EditRecipe.routeWithArgs &&
                                       !currentRoute.orEmpty().startsWith(EditRecipe.route)
                    if(shouldShowFab) {
                        FloatingActionButton(
                            onClick = { navController.navigateSingleTopTo(CreateRecipe.route) },
                            containerColor = Primary,
                            contentColor = Color.White,
                            modifier = Modifier
                                .padding(end = (LocalConfiguration.current.screenWidthDp.dp / 2 - 44.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add"
                            )
                        }
                    }
                }
            }
        )
        { innerPadding ->
            // Main content of the app goes here
            NavHost(
                navController = navController,
                startDestination = Home.route,
                Modifier.padding(innerPadding)) {
                composable(route = Home.route) {
                    if (auth != null) {
                        HomeScreen(
                            auth,
                            {navController.navigateSingleTopTo(Discover.route)},
                            {navController.navigateSingleTopTo(Saved.route)},
                            {
                                navController.popBackStack()
                                navController.navigateSingleTopTo(Home.route)
                            }
                        )
                    }
                }
                composable(route = Saved.route) {
                    SavedScreen(savedRecipes.value, onRecipeClick = {recipe ->
                        // Handle recipe click, navigate to a detailed view
                        navController.navigate("${SingleRecipe.route}/${recipe.id}")
                    })
                }
                composable(route = Search.route) {
                    SearchScreen(recipes.value, onRecipeClick = {recipe ->
                    // Handle recipe click, navigate to a detailed view
                    navController.navigate("${SingleRecipe.route}/${recipe.id}")
                    })
                }
                composable(route = Discover.route) {
                    DiscoverScreen(
                        recipes = recipes.value,
                        model = recipeViewModel,
                        onRecipeClick = { recipe ->
                            // Handle recipe click, navigate to a detailed view
                            navController.navigate("${SingleRecipe.route}/${recipe.id}")
                        },
                        navigator = navController,
                        currentSaves = savedRecipes.value
                    )
                }
                composable(route = Settings.route) {
                    SettingsScreen(
                        displayName = Firebase.auth.currentUser?.displayName,
                        onEditDisplayName = {name -> AccountServiceImpl().editDisplayName(name,
                            { print("hey") })},
                        signOut = {
                            AccountServiceImpl().signOut()
                            navController.popBackStack(navController.graph.startDestinationId, false)
                            navController.navigateSingleTopTo(Home.route)
                        },
                        recipes = recipes.value.filter {recipe -> recipe.owner == Firebase.auth.currentUser?.uid}
                        ,
                        onRecipeClick = {recipe -> navController.navigate("${SingleRecipe.route}/${recipe.id}") },
                        savedRecipes = savedRecipes.value
                    )
                }
                composable(route = CreateRecipe.route) {
                    CreateRecipeScreen(
                        onCreate = { recipe ->
                            // Handle recipe creation, e.g., save to database
                            writeRecipeToDatabase(
                                database,
                                recipe,
                                onSuccess = { savedRecipe ->
                                    // Add the new recipe to local state immediately
                                    recipeViewModel.setRecipes(recipes.value + savedRecipe)
                                    // Navigate to the newly created recipe's detail screen only after successful save
                                    // Pop CreateRecipe from back stack to ensure fresh state on next create
                                    navController.navigate("${SingleRecipe.route}/${savedRecipe.id}") {
                                        popUpTo(CreateRecipe.route) {
                                            inclusive = true
                                        }
                                    }
                                },
                                onFailure = { exception ->
                                    // Handle failure (could show a toast/snackbar)
                                    println("Failed to create recipe: ${exception.message}")
                                }
                            )
                        }

                    )
                }
                composable(
                    route = SingleRecipe.routeWithArgs ,
                    arguments = SingleRecipe.arguments

                ) {
                    val recipeId = it.arguments?.getInt(SingleRecipe.recipeIdArg)
                    val recipe = recipes.value.find { it.id == recipeId }
                    recipe?.let {
                        SingleRecipeScreen(recipe = it, model = recipeViewModel, onEditClick = {recipe ->
                            // Handle recipe click, navigate to a detailed view
                            navController.navigate("${EditRecipe.route}/${recipe.id}")
                        })
                    }
                }
                composable(
                    route = EditRecipe.routeWithArgs ,
                    arguments = EditRecipe.arguments

                ) {
                    val recipeId = it.arguments?.getInt(EditRecipe.recipeIdArg)
                    val recipe = recipes.value.find { it.id == recipeId }
                    recipe?.let {
                        EditRecipeScreen(recipe = it, model = recipeViewModel, onEdit = {recipe ->
                            updateRecipeInDatabase(
                                database,
                                recipe,
                                onSuccess = { updatedRecipe ->
                                    // Update the recipe in local state immediately
                                    val updatedList = recipes.value.map { existingRecipe ->
                                        if (existingRecipe.id == updatedRecipe.id) updatedRecipe else existingRecipe
                                    }
                                    recipeViewModel.setRecipes(updatedList)
                                    // Navigate to the recipe detail screen only after successful update
                                    // Pop EditRecipe from back stack to prevent navigation issues
                                    navController.navigate("${SingleRecipe.route}/${updatedRecipe.id}") {
                                        popUpTo(EditRecipe.routeWithArgs) {
                                            inclusive = true
                                        }
                                    }
                                },
                                onFailure = { exception ->
                                    // Handle failure (could show a toast/snackbar)
                                    println("Failed to update recipe: ${exception.message}")
                                }
                            )
                        })
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun RecipeFinderAppPreview() {
    RecipeFinderApp(database = Firebase.firestore, null)
}