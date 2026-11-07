package com.langly.langly_android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabWordDao {

    @Insert
    suspend fun insert(word: VocabWord): Long

    @Query("SELECT * FROM vocab_words ORDER BY savedAt DESC")
    fun getAll(): Flow<List<VocabWord>>

    @Query(
        "SELECT * FROM vocab_words " +
                "WHERE word LIKE '%' || :query || '%' " +
                "OR meaning LIKE '%' || :query || '%' " +
                "ORDER BY savedAt DESC"
    )
    fun search(query: String): Flow<List<VocabWord>>

    @Query("SELECT * FROM vocab_words WHERE id = :id")
    suspend fun getById(id: Long): VocabWord?

    @Query("SELECT COUNT(*) FROM vocab_words WHERE LOWER(word) = LOWER(:word)")
    suspend fun countByWord(word: String): Int

    @Query("UPDATE vocab_words SET meaning = :meaning, note = :note WHERE id = :id")
    suspend fun update(id: Long, meaning: String, note: String)

    @Query("DELETE FROM vocab_words WHERE id = :id")
    suspend fun delete(id: Long)
}