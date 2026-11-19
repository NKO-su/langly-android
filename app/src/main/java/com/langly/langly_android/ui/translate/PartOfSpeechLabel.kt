package com.langly.langly_android.ui.translate

import com.langly.langly_android.dictionary.PartOfSpeech

internal fun PartOfSpeech.label(): String = when (this) {
    PartOfSpeech.NOUN -> "danh từ"
    PartOfSpeech.VERB -> "động từ"
    PartOfSpeech.ADJECTIVE -> "tính từ"
    PartOfSpeech.OTHER -> "khác"
}