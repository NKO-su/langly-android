package com.langly.langly_android.dictionary

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DictionaryDatabase {

    private const val QUERY = """
        SELECT w.word, d.definition, d.pos, wd.example
        FROM words w
        JOIN word_definitions wd ON wd.word_id = w.id
        JOIN definitions d       ON d.id = wd.definition_id
        WHERE w.word = ? AND w.lang_code = 'en'
    """

    suspend fun lookup(context: Context, word: String): List<DictionaryMeaning> {
        val term = word.trim().lowercase()
        if (term.isEmpty()) return emptyList()

        return withContext(Dispatchers.IO) {
            val file = DictionaryInstaller.dictionaryFile(context)
            if (!file.exists()) return@withContext emptyList<DictionaryMeaning>()

            val result = mutableListOf<DictionaryMeaning>()
            SQLiteDatabase
                .openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
                .use { db ->
                    db.rawQuery(QUERY, arrayOf(term)).use { cursor ->
                        while (cursor.moveToNext()) {
                            result.add(
                                DictionaryMeaning(
                                    headword = cursor.getString(0),
                                    definition = cursor.getString(1),
                                    partOfSpeech = partOfSpeechFromRaw(cursor.getString(2)),
                                    example = cursor.getString(3)
                                )
                            )
                        }
                    }
                }
            result
        }
    }
}