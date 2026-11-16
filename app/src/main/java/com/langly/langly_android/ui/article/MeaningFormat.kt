package com.langly.langly_android.ui.article

private val WHITESPACE = Regex("\\s+")

fun cleanMeaning(raw: String): String = raw.replace(WHITESPACE, " ").trim()

fun joinMeanings(meanings: List<String>): String =
    meanings.map(::cleanMeaning).filter { it.isNotEmpty() }.joinToString("\n")

fun appendMeaning(existing: String, newMeaning: String): String {
    val cleaned = cleanMeaning(newMeaning)
    if (cleaned.isEmpty()) return existing
    return if (existing.isBlank()) cleaned else existing.trimEnd() + "\n" + cleaned
}

fun splitMeanings(stored: String): List<String> =
    stored.split("\n").map { it.trim() }.filter { it.isNotEmpty() }