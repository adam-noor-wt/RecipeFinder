package com.example.recipefinder.data

// Produces a grouping key for fuzzy-ish ingredient matching: case/whitespace-insensitive
// with simple suffix-based singular/plural collapsing (e.g. "Tomato"/"Tomatoes" -> "tomato").
fun normalizeIngredientKey(name: String): String {
    val trimmed = name.trim().lowercase()
    return when {
        trimmed.endsWith("sses") -> trimmed
        trimmed.endsWith("es") && trimmed.length > 3 -> trimmed.dropLast(2)
        trimmed.endsWith("ss") -> trimmed
        trimmed.endsWith("s") && trimmed.length > 1 -> trimmed.dropLast(1)
        else -> trimmed
    }
}
