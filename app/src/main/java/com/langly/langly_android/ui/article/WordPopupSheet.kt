package com.langly.langly_android.ui.article

import android.widget.Toast
import androidx.compose.foundation.clickable
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

private const val PREVIEW_MEANINGS = 2

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
                Toast.makeText(context, "Đã lưu \"${word.trim()}\"", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun WordPopupContent(
    state: WordPopupState,
    onSave: (word: String, meaning: String, note: String) -> Unit
) {
    var word by remember(state.text) { mutableStateOf(state.text) }
    var meaning by remember(state.text, state.isLoading) {
        mutableStateOf(
            state.meanings.take(PREVIEW_MEANINGS).joinToString("; ") { it.definition.orEmpty() }
        )
    }
    var note by remember(state.text) { mutableStateOf("") }
    var showAll by remember(state.text) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = word,
            onValueChange = { word = it },
            label = { Text("Từ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

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
                "Bạn đang chọn nhiều từ: hãy tự nhập nghĩa.",
                style = MaterialTheme.typography.bodySmall
            )
            state.meanings.isEmpty() -> Text(
                "Không tìm thấy trong từ điển: hãy tự nhập nghĩa.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = meaning,
            onValueChange = { meaning = it },
            label = { Text("Nghĩa") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Ghi chú") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.meanings.size > PREVIEW_MEANINGS) {
            TextButton(onClick = { showAll = !showAll }) {
                Text(if (showAll) "Ẩn bớt" else "Xem tất cả nghĩa (${state.meanings.size})")
            }
        }

        if (showAll) {
            Text(
                "Chạm vào một nghĩa để thêm vào ô Nghĩa.",
                style = MaterialTheme.typography.bodySmall
            )
            state.meanings.forEach { m ->
                val definition = m.definition.orEmpty()
                val pos = m.partOfSpeech?.toString().orEmpty()
                val example = m.example.orEmpty()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            meaning = if (meaning.isBlank()) definition else "$meaning; $definition"
                        }
                        .padding(vertical = 8.dp)
                ) {
                    if (pos.isNotBlank()) {
                        Text(pos, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(definition, style = MaterialTheme.typography.bodyMedium)
                    if (example.isNotBlank()) {
                        Text(example, style = MaterialTheme.typography.bodySmall)
                    }
                }
                HorizontalDivider()
            }
        }

        Button(
            onClick = { onSave(word, meaning, note) },
            enabled = word.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lưu")
        }
    }
}