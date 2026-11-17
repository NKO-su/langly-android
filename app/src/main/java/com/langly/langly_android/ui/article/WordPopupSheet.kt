package com.langly.langly_android.ui.article

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.langly.langly_android.ui.translate.WordPopupState
import com.langly.langly_android.ui.translate.WordPopupViewModel
import com.langly.langly_android.ui.translate.cleanMeaning
import com.langly.langly_android.ui.translate.joinMeanings

private const val PREVIEW_MEANINGS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordPopupSheet(viewModel: WordPopupViewModel) {
    val state by viewModel.state.collectAsState()
    val current = state ?: return
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = viewModel::dismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        WordPopupContent(
            state = current,
            onSave = { word, meaning, note ->
                viewModel.save(word, meaning, note)
                Toast.makeText(context, "Đã lưu \"$word\"", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun WordPopupContent(
    state: WordPopupState,
    onSave: (word: String, meaning: String, note: String) -> Unit
) {
    val entryWord = state.meanings.firstOrNull()?.headword ?: state.text
    val meaningText = remember(state.meanings) {
        joinMeanings(state.meanings.map { it.definition.orEmpty() })
    }
    var note by remember(state.text) { mutableStateOf("") }
    var showAll by remember(state.text) { mutableStateOf(false) }
    val shown = if (showAll) state.meanings else state.meanings.take(PREVIEW_MEANINGS)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = entryWord, style = MaterialTheme.typography.headlineSmall)

        if (entryWord != state.text && state.meanings.isNotEmpty()) {
            Text(
                text = "Từ bạn chọn: \"${state.text}\"",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (state.savedCount > 0) {
            Text(
                text = "Bạn đã lưu từ này ${state.savedCount} lần (vẫn có thể lưu thêm).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        when {
            state.isLoading -> CircularProgressIndicator()
            state.isPhrase -> Text(
                "Bạn đang chọn nhiều từ: chưa có bản dịch cho cụm. Bạn vẫn lưu được kèm ghi chú.",
                style = MaterialTheme.typography.bodySmall
            )
            state.meanings.isEmpty() -> Text(
                "Không tìm thấy trong từ điển. Bạn vẫn lưu được kèm ghi chú.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        shown.forEach { m ->
            val pos = m.partOfSpeech.toString()
            val example = m.example.orEmpty()
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(pos, style = MaterialTheme.typography.labelSmall)
                Text(
                    text = "• " + cleanMeaning(m.definition.orEmpty()),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (showAll && example.isNotBlank()) {
                    Text(example, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (state.meanings.size > PREVIEW_MEANINGS) {
            TextButton(onClick = { showAll = !showAll }) {
                Text(if (showAll) "Ẩn bớt" else "Xem tất cả nghĩa (${state.meanings.size})")
            }
        }

        HorizontalDivider()

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Ghi chú") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onSave(entryWord, meaningText, note) },
            enabled = !state.isLoading && entryWord.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lưu")
        }
    }
}