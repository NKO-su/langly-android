package com.langly.langly_android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocab_words")
data class VocabWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val meaning: String = "",
    val note: String = "",
    val savedAt: Long
)