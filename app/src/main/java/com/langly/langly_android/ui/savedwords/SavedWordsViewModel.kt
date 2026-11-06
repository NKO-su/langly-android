package com.langly.langly_android.ui.savedwords

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.langly.langly_android.data.local.AppDatabase
import com.langly.langly_android.data.local.VocabWord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedWordsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).vocabWordDao()

    val words: StateFlow<List<VocabWord>?> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addSampleWord() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            dao.insert(
                VocabWord(
                    word = "sample${now % 1000}",
                    meaning = "ádfgdsfgsdftfhgdhgfhggfhfgfhhhhhhhhhhhhhhhhhhhhhhhhhhhhh",
                    savedAt = now
                )
            )
        }
    }
}