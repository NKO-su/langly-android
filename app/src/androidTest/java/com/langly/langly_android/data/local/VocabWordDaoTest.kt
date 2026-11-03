package com.langly.langly_android.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VocabWordDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: VocabWordDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.vocabWordDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_thenGetById_returnsSameWord() = runBlocking {
        val id = dao.insert(VocabWord(word = "run", meaning = "chạy", savedAt = 1L))

        val loaded = dao.getById(id)

        assertNotNull(loaded)
        assertEquals("run", loaded!!.word)
        assertEquals("chạy", loaded.meaning)
    }

    @Test
    fun getAll_emitsAgain_afterInsert() = runBlocking {
        val emissions = CopyOnWriteArrayList<List<VocabWord>>()

        val job = launch(Dispatchers.IO) {
            dao.getAll().take(2).toCollection(emissions)
        }

        // Đợi lần phát đầu tiên (danh sách rỗng) rồi mới insert
        withTimeout(3000) {
            while (emissions.isEmpty()) delay(10)
        }
        dao.insert(VocabWord(word = "run", savedAt = 1L))
        withTimeout(3000) { job.join() }

        assertEquals(2, emissions.size)
        assertEquals(0, emissions[0].size)
        assertEquals("run", emissions[1][0].word)
    }
}