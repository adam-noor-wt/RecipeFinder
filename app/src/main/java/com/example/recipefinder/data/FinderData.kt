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
enum class Tag {
    VEGETARIAN, VEGAN, GLUTEN_FREE, DAIRY_FREE, PASTA, DESSERT, SALAD, SOUP, PROTEIN, TOFU, CURRY, SPICY, SWEET, SAVORY, BREAKFAST, LUNCH, DINNER, SNACK, SMOOTHIE
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