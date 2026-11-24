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
import com.example.recipefinder.database.readFoldersFromDatabase
import com.example.recipefinder.database.updateFoldersInDatabase
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
import com.example.recipefinder.ui.social.SocialScreen
import com.example.recipefinder.ui.social.FollowedUser
import com.example.recipefinder.ui.social.visibleName
import com.example.recipefinder.ui.profile.ProfileScreen
import com.example.recipefinder.ui.recipes.CreatorInfo
import com.example.recipefinder.data.PublicProfile
import com.example.recipefinder.database.*
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.ListenerRegistration
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
    recipeViewModel.setHasLoaded(true)
}

fun fetchRecipes(database: FirebaseFirestore) {
    // Simulate fetching recipes from a database
    readRecipesFromDatabase(db = database,
        { foundRecipes ->
            readSavedRecipesFromDatabase(database, foundRecipes, Firebase.auth.currentUser?.uid.toString()) { savedRecipes ->
                // Update the ViewModel with the fetched recipes
                getDatabaseRecipes(foundRecipes, savedRecipes)
                Firebase.auth.currentUser?.uid?.let { uid ->
                    readFoldersFromDatabase(database, uid) { recipeViewModel.setFolders(it) }
                }
            }
        },
        {
            // Handle error case, e.g., show a message to the user
            println("Error fetching recipes: $it")
            // Stop the spinner; screens show their "No ... recipes found" message
            recipeViewModel.setHasLoaded(true)
        }
    )
}

/** Loads follows, viewed recipes and the public profiles needed by the social features. */
fun loadSocialState(database: FirebaseFirestore) {
    val user = Firebase.auth.currentUser ?: return
    if (user.isAnonymous) {
        recipeViewModel.clearSocial()
        recipeViewModel.setSocialLoaded(true)
        return
    }
    readSocialState(database, user.uid) { state ->
        recipeViewModel.setFollows(state.follows)
        recipeViewModel.setViewed(state.viewed)
        readPublicProfiles(database, state.follows + user.uid) {
            recipeViewModel.putProfiles(it)
            recipeViewModel.setSocialLoaded(true)
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun RecipeFinderApp(database: FirebaseFirestore, auth: FirebaseAuth?) {
    val recipes = recipeViewModel.recipes.collectAsState()
    val savedRecipes = recipeViewModel.savedRecipes.collectAsState()
    val folders = recipeViewModel.folders.collectAsState()
    val hasLoaded = recipeViewModel.hasLoaded.collectAsState()
    val follows = recipeViewModel.follows.collectAsState()
    val viewed = recipeViewModel.viewed.collectAsState()
    val profiles = recipeViewModel.profiles.collectAsState()
    val socialLoaded = recipeViewModel.socialLoaded.collectAsState()
    val context = LocalContext.current

    // Followed users' recipes the current user hasn't viewed yet (private users never notify)
    val unviewedByUser = follows.value.mapNotNull { uid ->
        if (profiles.value[uid]?.isPublic != true) return@mapNotNull null
        val ids = recipes.value.filter { it.owner == uid && it.id !in viewed.value }.map { it.id }
        if (ids.isEmpty()) null else uid to ids
    }.toMap()
    val unviewedTotal = unviewedByUser.values.sumOf { it.size }

    // Real-time recipe updates + profile backfill when auth state changes
    DisposableEffect(auth) {
        var registration: ListenerRegistration? = null
        val listener = FirebaseAuth.AuthStateListener { a ->
            registration?.remove()
            registration = null
            val user = a.currentUser
            if (user != null) {
                if (!user.isAnonymous) ensurePublicProfile(database, user.uid, user.displayName)
                loadSocialState(database)
                registration = listenToRecipes(database) { recipeViewModel.setRecipes(it) }
            } else {
                recipeViewModel.clearSocial()
            }
        }
        auth?.addAuthStateListener(listener)
        onDispose {
            auth?.removeAuthStateListener(listener)
            registration?.remove()
        }
    }

    // One toast per followed user with newly created, unviewed recipes
    val toasted = remember { mutableSetOf<Int>() }
    LaunchedEffect(unviewedByUser, socialLoaded.value) {
        if (!socialLoaded.value) return@LaunchedEffect
        unviewedByUser.forEach { (uid, ids) ->
            if (ids.any { it !in toasted }) {
                toasted.addAll(ids)
                Toast.makeText(context, "${profiles.value[uid].visibleName()} created a new recipe", Toast.LENGTH_LONG).show()
            }
        }
    }
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
                        currentScreen = currentScreen,
                        badges = mapOf(Social.route to unviewedTotal)
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
                                        currentRoute != Discover.route &&
                                        currentRoute != Social.route &&
                                        !currentRoute.orEmpty().startsWith(Profile.route) &&
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
                    val saveFolders = { updated: Map<String, List<Int>> ->
                        recipeViewModel.setFolders(updated)
                        Firebase.auth.currentUser?.uid?.let { updateFoldersInDatabase(database, it, updated) }
                    }
                    SavedScreen(
                        savedRecipes.value,
                        isLoaded = hasLoaded.value,
                        folders = folders.value,
                        onRecipeClick = {recipe ->
                            // Handle recipe click, navigate to a detailed view
                            navController.navigate("${SingleRecipe.route}/${recipe.id}")
                        },
                        onCreateFolder = { name -> saveFolders(folders.value + (name to emptyList())) },
                        onDeleteFolder = { name -> saveFolders(folders.value - name) },
                        onFolderRecipesChanged = { name, ids -> saveFolders(folders.value + (name to ids)) }
                    )
                }
                composable(route = Search.route) {
                    SearchScreen(recipes.value, isLoaded = hasLoaded.value, onRecipeClick = {recipe ->
                    // Handle recipe click, navigate to a detailed view
                    navController.navigate("${SingleRecipe.route}/${recipe.id}")
                    })
                }
                composable(route = Discover.route) {
                    DiscoverScreen(
                        recipes = recipes.value,
                        isLoaded = hasLoaded.value,
                        model = recipeViewModel,
                        onRecipeClick = { recipe ->
                            // Handle recipe click, navigate to a detailed view
                            navController.navigate("${SingleRecipe.route}/${recipe.id}")
                        },
                        navigator = navController,
                        currentSaves = savedRecipes.value
                    )
                }
                composable(route = Social.route) {
                    val me = Firebase.auth.currentUser
                    SocialScreen(
                        followed = follows.value.map { uid ->
                            FollowedUser(uid, profiles.value[uid], unviewedByUser[uid]?.size ?: 0)
                        },
                        onSearch = { query, onResult -> searchPublicProfiles(database, query, me?.uid, onResult) },
                        onUserClick = { uid -> navController.navigate("${Profile.route}/$uid") },
                        onUnfollow = { uid ->
                            me?.let { unfollowUser(database, it.uid, uid) }
                            recipeViewModel.setFollows(follows.value - uid)
                        }
                    )
                }
                composable(route = Profile.routeWithArgs, arguments = Profile.arguments) { entry ->
                    val uid = entry.arguments?.getString(Profile.uidArg) ?: return@composable
                    val me = Firebase.auth.currentUser
                    val owned = recipes.value.filter { it.owner == uid }
                    val isFollowing = uid in follows.value
                    LaunchedEffect(uid) {
                        if (profiles.value[uid] == null) readPublicProfile(database, uid) { p ->
                            p?.let { recipeViewModel.putProfiles(mapOf(uid to it)) }
                        }
                    }
                    // Opening a followed user's profile marks their recipes as viewed
                    LaunchedEffect(uid, owned.map { it.id }, isFollowing, socialLoaded.value) {
                        if (me == null || !isFollowing || !socialLoaded.value) return@LaunchedEffect
                        val fresh = owned.map { it.id }.filter { it !in viewed.value }
                        if (fresh.isNotEmpty()) {
                            recipeViewModel.setViewed(viewed.value + fresh)
                            markRecipesViewed(database, me.uid, fresh)
                        }
                    }
                    val isSelf = me?.uid == uid
                    val canFollow = me != null && !me.isAnonymous && !isSelf
                    ProfileScreen(
                        uid = uid,
                        displayName = if (isSelf) (me?.displayName ?: "Not set") else profiles.value[uid].visibleName(),
                        recipes = owned,
                        isFollowing = isFollowing,
                        onToggleFollow = if (!canFollow) null else {
                            {
                                if (isFollowing) {
                                    unfollowUser(database, me!!.uid, uid)
                                    recipeViewModel.setFollows(follows.value - uid)
                                } else {
                                    followUser(database, me!!.uid, uid, owned.map { it.id })
                                    recipeViewModel.setFollows(follows.value + uid)
                                    recipeViewModel.setViewed(viewed.value + owned.map { it.id })
                                }
                            }
                        },
                        onRecipeClick = { recipe -> navController.navigate("${SingleRecipe.route}/${recipe.id}") }
                    )
                }
                composable(route = Settings.route) {
                    val me = Firebase.auth.currentUser
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
                        savedRecipes = savedRecipes.value,
                        uid = me?.uid,
                        isPublic = if (me == null || me.isAnonymous) null else profiles.value[me.uid]?.isPublic ?: false,
                        onPublicChange = { checked ->
                            me?.let {
                                writePublicProfile(database, it.uid, displayName = it.displayName ?: "", isPublic = checked)
                                recipeViewModel.putProfiles(mapOf(it.uid to PublicProfile(it.uid, it.displayName ?: "", checked)))
                            }
                        }
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