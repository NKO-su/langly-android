package com.langly.langly_android.ui.savedwords

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.langly.langly_android.data.local.VocabWord
import com.langly.langly_android.ui.article.splitMeanings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedWordsScreen(
    modifier: Modifier = Modifier,
    viewModel: SavedWordsViewModel = viewModel()
) {
    val words by viewModel.words.collectAsState()
    val query by viewModel.query.collectAsState()
    val selectedWord by viewModel.selectedWord.collectAsState()

    val listState = rememberLazyListState()

    BackHandler(enabled = selectedWord != null) {
        viewModel.closeWord()
    }

    val word = selectedWord
    if (word != null) {
        WordDetailContent(
            word = word,
            onBack = viewModel::closeWord,
            onDelete = {
                viewModel.deleteWord(word.id)
                viewModel.closeWord()
            },
            modifier = modifier
        )
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Tìm từ hoặc nghĩa") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val list = words
                when {
                    list == null -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    list.isEmpty() -> {
                        Text(
                            text = if (query.isBlank()) "Chưa có từ nào được lưu"
                            else "Không tìm thấy từ nào",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(list, key = { it.id }) { item ->
                                val preview = splitMeanings(item.meaning).firstOrNull().orEmpty()
                                Column(
                                    modifier = Modifier.clickable { viewModel.openWord(item.id) }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = item.word,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (preview.isNotBlank()) {
                                            Text(
                                                text = preview,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordDetailContent(
    word: VocabWord,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmDelete by remember(word.id) { mutableStateOf(false) }

    val savedAtText = remember(word.savedAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(word.savedAt))
    }
    val meanings = remember(word.meaning) { splitMeanings(word.meaning) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Quay lại danh sách") }

        Text(
            text = word.word,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Đã lưu lúc $savedAtText",
            style = MaterialTheme.typography.bodySmall
        )

        if (meanings.isEmpty()) {
            Text(
                text = "Chưa có nghĩa",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                meanings.forEach { meaning ->
                    Text(
                        text = "• $meaning",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (word.note.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Ghi chú",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = word.note,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // THÊM: bấm lần 1 hỏi lại, bấm lần 2 mới xóa
        OutlinedButton(
            onClick = { if (confirmDelete) onDelete() else confirmDelete = true }
        ) {
            Text(
                text = if (confirmDelete) "Chắc chắn xóa?" else "Xóa từ",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}