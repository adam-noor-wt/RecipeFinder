package com.example.recipefinder.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

/**
 * Fetches and parses recipe data from URLs using schema.org Recipe markup
 */
class RecipeUrlFetcher {

    suspend fun fetchRecipe(url: String): Result<ParsedRecipe> = withContext(Dispatchers.IO) {
        try {
            val html = URL(url).readText()
            val recipe = parseRecipeFromHtml(html)

            if (recipe != null) {
                Result.success(recipe)
            } else {
                Result.failure(Exception("No recipe data found on this page"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseRecipeFromHtml(html: String): ParsedRecipe? {
        // Look for JSON-LD schema.org Recipe markup
        val jsonLdPattern = """<script[^>]+type\s*=\s*["']application/ld\+json["'][^>]*>(.*?)</script>""".toRegex(RegexOption.DOT_MATCHES_ALL)

        for (match in jsonLdPattern.findAll(html)) {
            val jsonText = match.groupValues[1].trim()
            try {
                val json = JSONObject(jsonText)
                val recipe = parseJsonLdRecipe(json)
                if (recipe != null) return recipe

                // Check if it's a graph with multiple items
                if (json.has("@graph")) {
                    val graph = json.getJSONArray("@graph")
                    for (i in 0 until graph.length()) {
                        val item = graph.getJSONObject(i)
                        val recipeFromGraph = parseJsonLdRecipe(item)
                        if (recipeFromGraph != null) return recipeFromGraph
                    }
                }
            } catch (e: Exception) {
                // Try parsing as array
                try {
                    val jsonArray = JSONArray(jsonText)
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val recipe = parseJsonLdRecipe(item)
                        if (recipe != null) return recipe
                    }
                } catch (e2: Exception) {
                    continue
                }
            }
        }

        return null
    }

    private fun parseJsonLdRecipe(json: JSONObject): ParsedRecipe? {
        val type = json.optString("@type", "")
        if (type != "Recipe") return null

        val title = json.optString("name", "").ifEmpty { return null }
        val description = json.optString("description", "")

        // Parse prep time (ISO 8601 duration format like PT30M)
        val prepTimeStr = json.optString("prepTime", "") + json.optString("totalTime", "")
        val prepTime = parseDuration(prepTimeStr)

        // Parse ingredients
        val ingredientsJson = json.optJSONArray("recipeIngredient") ?: JSONArray()
        val ingredients = mutableListOf<Ingredient>()
        for (i in 0 until ingredientsJson.length()) {
            val ingredientText = ingredientsJson.optString(i, "")
            if (ingredientText.isNotBlank()) {
                val parsed = parseIngredientFromText(ingredientText)
                ingredients.add(parsed)
            }
        }

        // Parse steps
        val stepsJson = json.optJSONArray("recipeInstructions") ?: JSONArray()
        val steps = mutableListOf<Step>()
        for (i in 0 until stepsJson.length()) {
            val stepItem = stepsJson.opt(i)
            val stepText = when (stepItem) {
                is JSONObject -> stepItem.optString("text", "")
                is String -> stepItem
                else -> ""
            }
            if (stepText.isNotBlank()) {
                steps.add(Step(
                    index = i + 1,
                    title = "Step ${i + 1}",
                    description = stepText
                ))
            }
        }

        if (ingredients.isEmpty() && steps.isEmpty()) return null

        return ParsedRecipe(
            title = title,
            description = description,
            preparationTime = prepTime,
            ingredients = ingredients,
            steps = steps
        )
    }

    private fun parseDuration(duration: String): Int {
        // Parse ISO 8601 duration (e.g., PT30M, PT1H30M)
        if (duration.isEmpty()) return 0

        var totalMinutes = 0
        val hourMatch = """(\d+)H""".toRegex().find(duration)
        val minuteMatch = """(\d+)M""".toRegex().find(duration)

        hourMatch?.groupValues?.get(1)?.toIntOrNull()?.let { totalMinutes += it * 60 }
        minuteMatch?.groupValues?.get(1)?.toIntOrNull()?.let { totalMinutes += it }

        return totalMinutes
    }

    private fun parseIngredientFromText(text: String): Ingredient {
        // Try to extract quantity, unit, and name from text like "2 cups flour"
        val parts = text.trim().split("""[\s,]+""".toRegex(), limit = 3)

        var quantity = 1.0
        var unit = Unit.PIECE
        var name = text

        if (parts.isNotEmpty()) {
            // Try to parse first part as quantity
            val firstPart = parts[0].replace("""[^\d./]""".toRegex(), "")
            quantity = parseFraction(firstPart)

            if (parts.size >= 2 && quantity > 0) {
                // Try to parse second part as unit
                val unitStr = parts[1].lowercase()
                unit = when {
                    unitStr.startsWith("cup") -> Unit.CUP
                    unitStr.startsWith("tsp") || unitStr.startsWith("teaspoon") -> Unit.TEASPOON
                    unitStr.startsWith("tbsp") || unitStr.startsWith("tablespoon") -> Unit.TABLESPOON
                    unitStr.startsWith("g") || unitStr == "gram" || unitStr == "grams" -> Unit.GRAM
                    unitStr.startsWith("ml") || unitStr.startsWith("milliliter") -> Unit.MILLILITER
                    unitStr.startsWith("l") || unitStr == "liter" || unitStr == "liters" -> Unit.LITER
                    unitStr.startsWith("pinch") || unitStr.startsWith("dash") -> Unit.DASH
                    unitStr.startsWith("whole") -> Unit.WHOLE
                    else -> Unit.PIECE
                }

                // The rest is the ingredient name
                if (parts.size >= 3) {
                    name = parts[2]
                } else if (unit == Unit.PIECE) {
                    // Unit wasn't recognized, so include it in the name
                    name = parts.drop(1).joinToString(" ")
                }
            } else if (quantity == 0.0) {
                // First part wasn't a number, use full text as name
                quantity = 1.0
                name = text
            }
        }

        return Ingredient(
            id = 0,
            name = name,
            quantity = quantity,
            unit = unit
        )
    }

    private fun parseFraction(str: String): Double {
        if (str.isEmpty()) return 0.0

        // Handle fractions like "1/2"
        if (str.contains('/')) {
            val parts = str.split('/')
            if (parts.size == 2) {
                val num = parts[0].toDoubleOrNull() ?: 0.0
                val denom = parts[1].toDoubleOrNull() ?: 1.0
                return if (denom != 0.0) num / denom else 0.0
            }
        }

        return str.toDoubleOrNull() ?: 0.0
    }
}