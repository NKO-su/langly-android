package com.langly.langly_android.dictionary

import android.content.Context

suspend fun lookupWithForms(context: Context, word: String): List<DictionaryMeaning> {
    val exact = DictionaryDatabase.lookup(context, word)
    if (exact.isNotEmpty()) return exact

    for (stem in candidateStems(word)) {
        val found = DictionaryDatabase.lookup(context, stem)
        if (found.isNotEmpty()) return found
    }
    return emptyList()
}