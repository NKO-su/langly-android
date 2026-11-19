package com.langly.langly_android.dictionary

import android.content.Context

suspend fun findBaseWord(context: Context, word: String): String? {
    for (stem in candidateStems(word)) {
        val headword = DictionaryDatabase.lookup(context, stem).firstOrNull()?.headword
        if (headword != null && !headword.equals(word, ignoreCase = true)) return headword
    }
    return null
}