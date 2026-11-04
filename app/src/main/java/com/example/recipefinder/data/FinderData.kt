package com.example.recipefinder.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


// Represents a recipe in the Recipe Finder application.
data class Recipe(
    val id: Int,
    val title: String,
    val description: String,
    val preparationTime: Int,
    val steps : List<Step> = emptyList(),
    val imageUrl: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val style: String = "",
    val tags: List<Tag> = emptyList(),
    val owner: String = "Anonymous"
)

// Represents an ingredient in a recipe.
data class Ingredient(
    val id: Int,
    val name: String,
    val quantity: Double,
    val unit: Unit,
    val note: String = ""
)

// Formats a quantity without a trailing ".0" for whole numbers.
fun Double.trimmedString(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()

// Formats a quantity as a whole number or simple fraction (denominator <= 10) when possible,
// e.g. 0.6666667 -> "2/3", 1.5 -> "1 1/2". Falls back to a 2-decimal-place string otherwise.
fun Double.fractionString(): String {
    val whole = this.toLong()
    if (this == whole.toDouble()) return whole.toString()

    val fractional = this - whole
    val tolerance = 0.0001
    for (denominator in 2..10) {
        val numerator = Math.round(fractional * denominator)
        if (numerator == 0L || numerator == denominator.toLong()) continue
        if (Math.abs(fractional - numerator.toDouble() / denominator) < tolerance) {
            val fractionPart = "$numerator/$denominator"
            return if (whole != 0L) "$whole $fractionPart" else fractionPart
        }
    }

    return "%.2f".format(this)
}

// Represents a step in the preparation of a recipe.
data class Step(
    val index: Int = 0,
    val title: String = "Something went wrong",
    val description: String = "",
    val imageUrl: String = ""
)

// Represents a tag that can be associated with a recipe.
enum class Tag(
    val displayName: String,
    val priority: Int,
    val category: TagCategory
) {
    // Meal types (highest priority)
    BREAKFAST("Breakfast", 100, TagCategory.MEAL_TYPE),
    LUNCH("Lunch", 90, TagCategory.MEAL_TYPE),
    DINNER("Dinner", 80, TagCategory.MEAL_TYPE),
    SNACK("Snack", 70, TagCategory.MEAL_TYPE),

    // Dish types
    DESSERT("Dessert", 60, TagCategory.DISH_TYPE),
    SOUP("Soup", 55, TagCategory.DISH_TYPE),
    SALAD("Salad", 54, TagCategory.DISH_TYPE),
    SMOOTHIE("Smoothie", 53, TagCategory.DISH_TYPE),
    SANDWICH("Sandwich", 52, TagCategory.DISH_TYPE),
    BAKED("Baked", 51, TagCategory.DISH_TYPE),

    // Cuisine/ingredients
    PASTA("Pasta", 50, TagCategory.CUISINE),
    CURRY("Curry", 49, TagCategory.CUISINE),

    // Proteins/ingredients
    PROTEIN("Protein", 40, TagCategory.INGREDIENT),
    TOFU("Tofu", 39, TagCategory.INGREDIENT),
    POULTRY("Poultry", 38, TagCategory.INGREDIENT),
    RED_MEAT("Red Meat", 37, TagCategory.INGREDIENT),
    SEAFOOD("Seafood", 36, TagCategory.INGREDIENT),
    PORK("Pork", 35, TagCategory.INGREDIENT),
    EGGS("Eggs", 34, TagCategory.INGREDIENT),
    CHEESE("Cheese", 33, TagCategory.INGREDIENT),
    VEGETABLES("Vegetables", 32, TagCategory.INGREDIENT),
    RICE("Rice", 31, TagCategory.INGREDIENT),

    // Flavor profiles
    SPICY("Spicy", 30, TagCategory.FLAVOR),
    SWEET("Sweet", 29, TagCategory.FLAVOR),
    SAVORY("Savory", 28, TagCategory.FLAVOR),

    // Dietary restrictions (lowest priority)
    VEGETARIAN("Vegetarian", 20, TagCategory.DIETARY),
    VEGAN("Vegan", 19, TagCategory.DIETARY),
    GLUTEN_FREE("Gluten Free", 18, TagCategory.DIETARY),
    DAIRY_FREE("Dairy Free", 17, TagCategory.DIETARY);

    companion object {
        fun sortedByPriority(tags: List<Tag>): List<Tag> =
            tags.sortedByDescending { it.priority }
    }
}

// Represents a category for tag grouping and organization.
enum class TagCategory {
    MEAL_TYPE, DISH_TYPE, CUISINE, INGREDIENT, FLAVOR, DIETARY
}

// Represents a unit of measurement for ingredients.
enum class Unit {
    GRAM, LITER, MILLILITER, CUP, TABLESPOON, TEASPOON, PIECE, WHOLE, DASH
}

fun abbreviateUnit(unit: Unit): String {
    return when (unit) {
        Unit.GRAM -> "g"
        Unit.LITER -> "L"
        Unit.MILLILITER -> "ml"
        Unit.CUP -> " cup"
        Unit.TABLESPOON -> "tbsp"
        Unit.TEASPOON -> "tsp"
        Unit.PIECE -> "pcs"
        Unit.WHOLE -> " whole"
        Unit.DASH -> " dash"
    }
}

// Find recipes that match tags, style, or ingredients.
fun findRecipesWithTags(
    recipes: List<Recipe>,
    tags: List<Tag>
): List<Recipe> {
    return recipes.filter { recipe ->
        tags.all { it in recipe.tags }
    }
}

fun findRecipesWithStyle(
    recipes: List<Recipe>,
    style: String
): List<Recipe> {
    return recipes.filter { recipe ->
        recipe.style == style
    }
}

fun findRecipesWithSomeIngredients(
    recipes: List<Recipe>,
    ingredients: List<String>
): List<Recipe> {
    return recipes.filter { recipe ->
        recipe.ingredients.any { ingredient ->
            ingredient.name in ingredients
        }
    }
}

fun findRecipesWithAllIngredients(
    recipes: List<Recipe>,
    ingredients: List<String>
): List<Recipe> {
    return recipes.filter { recipe ->
        ingredients.all { ingredient ->
            recipe.ingredients.any { it.name == ingredient }
        }
    }
}

fun findRecipesIMade(
    recipes: List<Recipe>
): List<Recipe> {
    return recipes.filter { recipe ->
        recipe.owner ==  Firebase.auth.currentUser?.uid
    }

}