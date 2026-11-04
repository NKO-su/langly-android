package com.langly.langly_android.dictionary

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

enum class DictionaryState { PREPARING, READY, FAILED }

object DictionaryInstaller {

    const val DB_NAME = "dictionary_en_vi.db"
    const val DICT_VERSION = 1

    private val _state = MutableStateFlow(DictionaryState.PREPARING)
    val state: StateFlow<DictionaryState> = _state.asStateFlow()

    fun dictionaryFile(context: Context): File {
        return context.getDatabasePath(DB_NAME)
    }

    fun tempFile(context: Context): File {
        val main = dictionaryFile(context)
        return File(main.path + ".tmp")
    }

    fun installedVersion(context: Context): Int {
        val file = dictionaryFile(context)
        if (!file.exists()) return 0
        return try {
            SQLiteDatabase
                .openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
                .use { db -> db.version }
        } catch (e: SQLiteException) {
            0
        }
    }

    fun copyFromAssets(context: Context) {
        val main = dictionaryFile(context)
        val tmp = tempFile(context)

        main.parentFile?.mkdirs()
        if (tmp.exists()) tmp.delete()

        context.assets.open(DB_NAME).use { input ->
            FileOutputStream(tmp).use { output ->
                input.copyTo(output)
            }
        }

        if (main.exists()) main.delete()
        if (!tmp.renameTo(main)) {
            throw IOException("Cannot rename ${tmp.path} to ${main.path}")
        }
    }

    suspend fun ensureInstalled(context: Context) {
        try {
            withContext(Dispatchers.IO) {
                if (installedVersion(context) < DICT_VERSION) {
                    copyFromAssets(context)
                }
            }
            _state.value = DictionaryState.READY
        } catch (e: Exception) {
            Log.e("Dictionary", "Install failed", e)
            _state.value = DictionaryState.FAILED
        }
    }
}