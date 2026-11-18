package com.langly.langly_android.ui.translate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.langly.langly_android.data.local.AppDatabase
import com.langly.langly_android.data.local.VocabWord
import com.langly.langly_android.dictionary.DictionaryMeaning
import com.langly.langly_android.dictionary.lookupWithForms
import kotlinx.coroutines.CancellationException
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
    val isError: Boolean = false,
    val savedCount: Int = 0,
    val fromInput: Boolean = false
) {
    val entryWord: String get() = meanings.firstOrNull()?.headword ?: text
}

class WordPopupViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).vocabWordDao()
    private val _state = MutableStateFlow<WordPopupState?>(null)
    val state: StateFlow<WordPopupState?> = _state
    private val _isCardOpen = MutableStateFlow(false)
    val isCardOpen: StateFlow<Boolean> = _isCardOpen

    private var lookupJob: Job? = null

    fun onTextSelected(raw: String) = lookup(raw, fromInput = false)

    fun onTextTyped(raw: String) = lookup(raw, fromInput = true)

    private fun lookup(raw: String, fromInput: Boolean) {
        val text = raw.trim { !it.isLetterOrDigit() }
        if (text.isEmpty()) return

        val isPhrase = text.any { it.isWhitespace() }

        lookupJob?.cancel()
        _isCardOpen.value = true
        _state.value = WordPopupState(
            text = text,
            isLoading = !isPhrase,
            isPhrase = isPhrase,
            fromInput = fromInput
        )

        lookupJob = viewModelScope.launch {
            val result = try {
                val meanings: List<DictionaryMeaning> =
                    if (isPhrase) emptyList() else lookupWithForms(getApplication(), text)
                val entryWord = meanings.firstOrNull()?.headword ?: text
                WordPopupState(
                    text = text,
                    meanings = meanings,
                    isLoading = false,
                    isPhrase = isPhrase,
                    savedCount = dao.countByWord(entryWord),
                    fromInput = fromInput
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                WordPopupState(
                    text = text,
                    isLoading = false,
                    isPhrase = isPhrase,
                    isError = true,
                    fromInput = fromInput
                )
            }

            _state.update { current -> if (current?.text == text) result else current }
        }
    }

    fun saveCurrent() {
        val current = _state.value ?: return
        if (current.isLoading || current.isPhrase || current.isError) return

        val entryWord = current.entryWord
        val meaning = joinMeanings(current.meanings.map { it.definition })

        viewModelScope.launch {
            dao.insert(
                VocabWord(
                    word = entryWord,
                    meaning = meaning,
                    note = "",
                    savedAt = System.currentTimeMillis()
                )
            )
            _state.update { cur ->
                if (cur != null && cur.text == current.text) cur.copy(savedCount = cur.savedCount + 1)
                else cur
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

    fun openCard() {
        _isCardOpen.value = true
    }

    fun newLookup() {
        lookupJob?.cancel()
        _state.value = null
    }

    fun dismiss() {
        lookupJob?.cancel()
        _state.value = null
        _isCardOpen.value = false
    }
}