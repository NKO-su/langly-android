package com.langly.langly_android.ui.savedwords

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.langly.langly_android.data.local.AppDatabase
import com.langly.langly_android.data.local.VocabWord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SavedWordsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).vocabWordDao()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val words: StateFlow<List<VocabWord>?> = _query
        .flatMapLatest { q ->
            if (q.isBlank()) dao.getAll() else dao.search(q.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _selectedWord = MutableStateFlow<VocabWord?>(null)
    val selectedWord: StateFlow<VocabWord?> = _selectedWord

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun openWord(id: Long) {
        viewModelScope.launch { _selectedWord.value = dao.getById(id) }
    }

    fun closeWord() {
        _selectedWord.value = null
    }

    fun updateWord(id: Long, meaning: String, note: String) {
        viewModelScope.launch { dao.update(id, meaning.trim(), note.trim()) }
    }

    fun deleteWord(id: Long) {
        viewModelScope.launch { dao.delete(id) }
    }

    fun addSampleWord() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            dao.insert(
                VocabWord(
                    word = "sample${now % 1000}",
                    meaning = "zsdfrhtrhrthrthfdgdfghjdfghgfhrthrthrthrthrthrthrthrthrthx",
                    savedAt = now
                )
            )
        }
    }
}