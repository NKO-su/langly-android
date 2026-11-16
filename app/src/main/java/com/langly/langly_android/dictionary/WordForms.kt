package com.langly.langly_android.dictionary

private const val MIN_STEM_LENGTH = 3
private const val VOWELS = "aeiou"

fun candidateStems(word: String): List<String> {
    val w = word.trim().lowercase()
    val found = LinkedHashSet<String>()

    fun add(stem: String) {
        if (stem.length >= MIN_STEM_LENGTH) found.add(stem)
    }

    fun addFromStem(stem: String) {
        add(stem + "e")
        add(stem)
        if (stem.length >= 2 && stem.last() == stem[stem.length - 2] && stem.last() !in VOWELS) {
            add(stem.dropLast(1))
        }
    }

    when {
        w.endsWith("ing") -> addFromStem(w.removeSuffix("ing"))
        w.endsWith("ied") -> {
            add(w.removeSuffix("ied") + "y")
            addFromStem(w.removeSuffix("ed"))
        }
        w.endsWith("ed") -> addFromStem(w.removeSuffix("ed"))
        w.endsWith("ies") -> {
            add(w.removeSuffix("ies") + "y")
            add(w.removeSuffix("s"))
        }
        w.endsWith("es") -> {
            add(w.removeSuffix("s"))
            add(w.removeSuffix("es"))
        }
        w.endsWith("s") && !w.endsWith("ss") -> add(w.removeSuffix("s"))
    }

    return found.sortedByDescending { it.length }
}