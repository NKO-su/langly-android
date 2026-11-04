package com.langly.langly_android.dictionary
enum class PartOfSpeech { NOUN, VERB, ADJECTIVE, OTHER }

data class DictionaryMeaning(
    val headword: String,
    val definition: String,
    val partOfSpeech: PartOfSpeech,
    val example: String?
)

fun partOfSpeechFromRaw(raw: String?): PartOfSpeech {
    return when (raw?.trim()) {
        "N", "n", "noun" -> PartOfSpeech.NOUN
        "V", "verb" -> PartOfSpeech.VERB
        "A", "adj" -> PartOfSpeech.ADJECTIVE
        else -> PartOfSpeech.OTHER
    }
}