package com.langly.langly_android.ui.article

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.langly.langly_android.data.local.AppDatabase
import com.langly.langly_android.data.local.VocabWord
import com.langly.langly_android.dictionary.DictionaryDatabase
import com.langly.langly_android.dictionary.DictionaryMeaning
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordPopupState(
    val text: String,
    val meanings: List<DictionaryMeaning> = emptyList(),
    val isLoading: Boolean = true,
    val isPhrase: Boolean = false,
    val savedCount: Int = 0
)

class WordPopupViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).vocabWordDao()

    private val _state = MutableStateFlow<WordPopupState?>(null)
    val state: StateFlow<WordPopupState?> = _state

    private var lookupJob: Job? = null

    fun onTextSelected(raw: String) {
        val text = raw.trim { !it.isLetterOrDigit() }
        if (text.isEmpty()) return

        val isPhrase = text.any { it.isWhitespace() }

        lookupJob?.cancel()
        _state.value = WordPopupState(text = text, isLoading = !isPhrase, isPhrase = isPhrase)

        lookupJob = viewModelScope.launch {
            val savedCount = dao.countByWord(text)
            val meanings = if (isPhrase) emptyList()
            else DictionaryDatabase.lookup(getApplication(), text)

            _state.update { current ->
                if (current?.text == text) {
                    WordPopupState(
                        text = text,
                        meanings = meanings,
                        isLoading = false,
                        isPhrase = isPhrase,
                        savedCount = savedCount
                    )
                } else {
                    current
                }
            }
        }
    }

    fun save(word: String, meaning: String, note: String) {
        val trimmedWord = word.trim()
        if (trimmedWord.isEmpty()) return
        viewModelScope.launch {
            dao.insert(
                VocabWord(
                    word = trimmedWord,
                    meaning = meaning.trim(),
                    note = note.trim(),
                    savedAt = System.currentTimeMillis()
                )
            )
            _state.value = null
        }
    }

    fun dismiss() {
        lookupJob?.cancel()
        _state.value = null
    }
}