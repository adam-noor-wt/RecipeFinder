package com.example.recipefinder.data

data class ParsedRecipe(
    val title: String = "",
    val description: String = "",
    val preparationTime: Int = 0,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<Step> = emptyList(),
)

private val NUMBERED_LINE = Regex("""^(?:step\s*)?(\d+)[.)]\s*(.*)$""", RegexOption.IGNORE_CASE)
private val TIME_PATTERN = Regex("""(\d+)\s*(hours?|hrs?|minutes?|mins?)""", RegexOption.IGNORE_CASE)
private val BULLET_PREFIX = Regex("""^[•*\-]\s*""")
private val INGREDIENT_LINE = Regex("""^(\d+(?:[./]\d+)?)\s*([a-zA-Z]+)?\s*(.+)$""")

private val UNIT_SYNONYMS: Map<String, Unit> = buildMap {
    fun register(unit: Unit, vararg words: String) {
        words.forEach { put(it.lowercase(), unit) }
    }
    register(Unit.GRAM, "g", "gram", "grams", "gr")
    register(Unit.LITER, "l", "liter", "liters", "litre", "litres")
    register(Unit.MILLILITER, "ml", "milliliter", "milliliters", "millilitre", "millilitres")
    register(Unit.CUP, "c", "cup", "cups")
    register(Unit.TABLESPOON, "tbsp", "tbsps", "tablespoon", "tablespoons")
    register(Unit.TEASPOON, "tsp", "tsps", "teaspoon", "teaspoons")
    register(Unit.PIECE, "piece", "pieces", "pc", "pcs")
    register(Unit.WHOLE, "whole")
    register(Unit.DASH, "dash", "dashes")
}

private fun parseQuantity(text: String): Double? {
    if (text.contains("/")) {
        val (numStr, denStr) = text.split("/", limit = 2)
        val num = numStr.toDoubleOrNull() ?: return null
        val den = denStr.toDoubleOrNull() ?: return null
        if (den == 0.0) return null
        return num / den
    }
    return text.toDoubleOrNull()
}

fun parseRecipeText(text: String): ParsedRecipe {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    if (lines.isEmpty()) return ParsedRecipe()

    val stepIndices = findStepRun(lines)
    val steps = stepIndices.mapIndexedNotNull { position, lineIndex ->
        val match = NUMBERED_LINE.matchEntire(lines[lineIndex]) ?: return@mapIndexedNotNull null
        val content = match.groupValues[2].trim()
        if (content.isEmpty()) return@mapIndexedNotNull null
        val (stepTitle, stepDescription) = splitStepContent(content)
        Step(index = position + 1, title = stepTitle, description = stepDescription)
    }

    val remainingIndices = lines.indices.filterNot { it in stepIndices }
    val ingredients = mutableListOf<Ingredient>()
    val ingredientIndices = mutableSetOf<Int>()
    for (i in remainingIndices) {
        val ingredient = parseIngredientLine(lines[i]) ?: continue
        ingredients.add(ingredient)
        ingredientIndices.add(i)
    }

    val titleIndex = lines.indices.firstOrNull { it !in stepIndices && it !in ingredientIndices }
    val title = titleIndex?.let { lines[it] }.orEmpty().take(80)

    val descriptionLines = lines.indices
        .filter { it !in stepIndices && it !in ingredientIndices && it != titleIndex }
        .takeWhile { it < (stepIndices.minOrNull() ?: Int.MAX_VALUE) && it < (ingredientIndices.minOrNull() ?: Int.MAX_VALUE) }
        .map { lines[it] }
    val description = descriptionLines.joinToString(" ").trim()

    val preparationTime = TIME_PATTERN.findAll(text).sumOf { match ->
        val amount = match.groupValues[1].toIntOrNull() ?: 0
        val unitWord = match.groupValues[2].lowercase()
        if (unitWord.startsWith("h")) amount * 60 else amount
    }

    return ParsedRecipe(
        title = title,
        description = description,
        preparationTime = preparationTime,
        ingredients = ingredients,
        steps = steps,
    )
}

private fun findStepRun(lines: List<String>): Set<Int> {
    var bestRun = emptyList<Int>()
    var i = 0
    while (i < lines.size) {
        val firstMatch = NUMBERED_LINE.matchEntire(lines[i])
        if (firstMatch == null) {
            i++
            continue
        }
        var expectedNumber = firstMatch.groupValues[1].toIntOrNull() ?: 1
        val run = mutableListOf(i)
        var j = i + 1
        while (j < lines.size) {
            val match = NUMBERED_LINE.matchEntire(lines[j])
            if (match != null) {
                val number = match.groupValues[1].toIntOrNull()
                if (number == expectedNumber + 1) {
                    expectedNumber = number
                    run.add(j)
                    j++
                    continue
                } else {
                    break
                }
            }
            j++
        }
        if (run.size > bestRun.size) bestRun = run
        i = j
    }
    return bestRun.toSet()
}

private fun splitStepContent(content: String): Pair<String, String> {
    val sentenceEnd = content.indexOf('.')
    val words = content.split(Regex("\\s+"))
    val title = when {
        sentenceEnd in 1 until content.length - 1 -> content.substring(0, sentenceEnd)
        words.size > 6 -> words.take(6).joinToString(" ")
        else -> content
    }
    return title to content
}

private fun parseIngredientLine(rawLine: String): Ingredient? {
    val line = rawLine.replace(BULLET_PREFIX, "").trim()
    val match = INGREDIENT_LINE.matchEntire(line) ?: return null

    val quantity = parseQuantity(match.groupValues[1]) ?: return null
    val unitWord = match.groupValues[2].trim().lowercase()
    val unit = UNIT_SYNONYMS[unitWord] ?: UNIT_SYNONYMS[unitWord.removeSuffix("s")] ?: return null

    val name = match.groupValues[3].trim()
    if (name.isEmpty()) return null

    return Ingredient(
        id = 0,
        name = name.lowercase().replaceFirstChar(Char::titlecase),
        quantity = quantity,
        unit = unit,
    )
}
