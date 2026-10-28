# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Recipedia (package `com.example.recipefinder`, Gradle root project `RecipeFinder`) is a single-module native Android app built with Kotlin and Jetpack Compose. It lets users browse, search, save, and create recipes, backed by Firebase (Auth + Firestore).

## Commands

Use the Gradle wrapper for everything; there is no separate lint/test runner script.

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (app/src/test)
./gradlew test

# Run a single unit test class/method
./gradlew test --tests "com.example.recipefinder.ExampleUnitTest"
./gradlew test --tests "com.example.recipefinder.ExampleUnitTest.methodName"

# Run instrumented tests (app/src/androidTest) — requires a connected device/emulator
./gradlew connectedAndroidTest

# Static analysis / lint
./gradlew lint

# Clean build
./gradlew clean
```

There are currently almost no tests beyond the default template (`ExampleUnitTest.kt`, `ExampleInstrumentedTest.kt`) — this codebase has essentially no automated test coverage.

## Firebase configuration

The app depends on `app/google-services.json` (Firebase project config) and initializes `FirebaseAuth` and `FirebaseFirestore` in `MainActivity`. Firestore is the source of truth for recipes and saved-recipe state; there is no local persistence layer in active use (see Architecture notes below on the unused Room dependency).

Firestore collections:
- `RECIPES` — one document per recipe, shaped as `SafeRecipe` (see Data layer below).
- `USERS/{uid}` — per-user document with a `savedRecipes: List<String>` field (recipe IDs as strings).

## Architecture

### Navigation & app shell

`RecipeActivity.kt` (`MainActivity` + the `RecipeFinderApp` composable) is the app's single entry point and owns the top-level `NavHost`, bottom tab bar, and FAB. There is no per-screen ViewModel-driven navigation — navigation destinations are defined centrally in `RecipeDestinations.kt` as objects implementing `RecipeDestination` (icon + route), collected into `recipeDestinations` for the tab row. `SingleRecipe` is the one destination that takes a nav argument (`recipe_id`, an `Int`) and is not part of the tab row.

Screens live under `ui/<feature>/` (`home`, `search`, `market`, `saved`, `settings`, `createrecipe`, `recipes`, `login`) and are plain `@Composable` functions that receive data and callbacks as parameters — they do not fetch data themselves. `RecipeActivity.kt` wires each screen to the shared `recipeViewModel` state and to Firestore read/write calls.

Auth gating is done inline by checking `auth.currentUser` — e.g. `HomeScreen` shows `LoginModal` when signed out, and the bottom bar / FAB in `RecipeFinderApp` are hidden entirely when there's no signed-in user.

### State management

There is a single app-wide `RecipeViewModel` instance (`recipeViewModel`, instantiated as a top-level `val` in `RecipeActivity.kt`, not scoped via `viewModel()`) exposing `recipes` and `savedRecipes` as `StateFlow<List<Recipe>>`. Data flows one way: Firestore → `fetchRecipes()` → `recipeViewModel.setRecipes()/setSavedRecipes()` → screens via `collectAsState()`. Screens navigate to `SingleRecipe` by ID and look the recipe up from the already-loaded `recipes` StateFlow rather than re-fetching.

`fetchRecipes()` is called on every bottom-tab navigation (see the `onTabSelected` callback in `RecipeFinderApp`), not just once at startup — this is the app's de facto refresh mechanism.

Login state (`LoginViewModel`) is separate and local to the login flow, using `mutableStateOf(LoginUiState)` rather than `StateFlow`.

### Data model vs. database model

`data/FinderData.kt` defines the app's domain model (`Recipe`, `Ingredient`, `Step`, `Tag` enum, `Unit` enum) plus pure filter helpers (`findRecipesWithTags`, `findRecipesWithStyle`, `findRecipesWithSomeIngredients`, `findRecipesWithAllIngredients`).

`database/database.kt` defines a **parallel** `SafeRecipe`/`SafeIngredient`/`SafeTag` set of data classes used only for Firestore (de)serialization (enums become strings, everything has defaults so Firestore's reflection-based mapping works), plus converter functions `safeRecipeConverter`/`recipeConverter` between the two, and all Firestore read/write functions (`writeRecipeToDatabase`, `readRecipesFromDatabase`, `readSavedRecipesFromDatabase`, `updateSavedRecipesInDatabase`, `updateRecipeInDatabase`). When adding fields to `Recipe`, update both data classes and both converters.

Note: `androidx.room` is a declared dependency (`libs.androidx.room.runtime`, `libs.androidx.room.common.jvm`) but there is no Room `@Entity`/`@Dao`/`@Database` code anywhere — persistence is Firestore-only today.

### Auth

`ui/login/AccountService` is an interface with a single implementation, `AccountServiceImpl`, which wraps `FirebaseAuth` directly (no DI framework — call sites `new` the impl directly, e.g. in `LoginViewModel` and in the sign-out handler in `RecipeActivity.kt`). It supports anonymous sign-in, email/password sign-in and sign-up, linking an anonymous account to email/password, sign-out, and display-name updates. New user Firestore documents (`USERS/{uid}` with empty `savedRecipes`) are created lazily on first successful email/password sign-in, not on account creation.

### Theming

`ui/theme/` follows the standard Compose Material3 template split (`Color.kt`, `Theme.kt`, `Type.kt`), but most screens bypass `MaterialTheme.colorScheme` and reference the named color constants (`Primary`, `Secondary`, `Tertiary`, etc. from `Color.kt`) directly rather than theme-relative colors — keep this in mind when making theme-aware or dark-mode changes, since many screens won't respond to a scheme swap automatically.
