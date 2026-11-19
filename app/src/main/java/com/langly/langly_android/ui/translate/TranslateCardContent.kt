package com.langly.langly_android.ui.translate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.langly.langly_android.dictionary.DictionaryMeaning
import com.langly.langly_android.dictionary.PartOfSpeech

private const val MAX_POS_GROUPS = 2
private const val MAX_PER_GROUP = 2

@Composable
internal fun TranslateCardContent(
    state: WordPopupState?,
    canExpand: Boolean,
    handleDrag: Modifier,
    onExpand: () -> Unit,
    onSubmit: (String) -> Unit,
    onSave: () -> Unit,
    onOpenBase: () -> Unit,
    onClose: () -> Unit
) {
    val showInput = state == null || state.fromInput

    Column(modifier = Modifier.fillMaxSize()) {
        if (canExpand) {
            GrabHandle(onClick = onExpand, dragModifier = handleDrag)
        } else {
            Spacer(modifier = Modifier.height(HANDLE_HEIGHT))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showInput) "Dịch" else state?.entryWord.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClose) { Text("Đóng") }
            }

            if (showInput) {
                LookupField(onSubmit = onSubmit, showHint = state == null)
            }

            if (state != null) {
                ResultSection(
                    state = state,
                    showWord = state.fromInput,
                    onSave = onSave,
                    onOpenBase = onOpenBase
                )
            }
        }
    }
}

@Composable
private fun LookupField(onSubmit: (String) -> Unit, showHint: Boolean) {
    var input by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text("Nhập từ hoặc cụm từ") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit(input) }),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    )
    if (showHint) {
        Text(
            text = "Hoặc nhấn vào một từ trong bài để tra.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.ResultSection(
    state: WordPopupState,
    showWord: Boolean,
    onSave: () -> Unit,
    onOpenBase: () -> Unit
) {
    val phonetic = state.meanings.firstOrNull()?.phonetic

    if (showWord) {
        Text(
            text = state.entryWord,
            style = MaterialTheme.typography.titleLarge
        )
    }

    if (!phonetic.isNullOrBlank()) {
        Text(
            text = phonetic,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (state.entryWord != state.text && state.meanings.isNotEmpty()) {
        Text(
            text = "Từ bạn chọn: \"${state.text}\"",
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (state.savedCount > 0) {
        Text(
            text = "Đã lưu ${state.savedCount} lần",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }

    state.baseWord?.let { base ->
        TextButton(onClick = onOpenBase) {
            Text("Xem từ gốc: $base")
        }
    }

    when {
        state.isLoading -> CircularProgressIndicator()
        state.isError -> Text(
            "Không tra được từ điển. Thử lại sau.",
            style = MaterialTheme.typography.bodySmall
        )
        state.isPhrase -> Text(
            "Cụm từ: bản dịch sẽ có ở bước sau.",
            style = MaterialTheme.typography.bodySmall
        )
        state.meanings.isEmpty() -> Text(
            "Không tìm thấy trong từ điển.",
            style = MaterialTheme.typography.bodySmall
        )
    }

    val groups = remember(state.meanings) { basicMeanings(state.meanings) }
    if (groups.isNotEmpty()) {
        Text(
            text = "Một số nghĩa cơ bản",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        groups.forEach { (pos, definitions) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(pos.label(), style = MaterialTheme.typography.labelSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    definitions.forEach { MeaningChip(it) }
                }
            }
        }
    }

    Spacer(modifier = Modifier.weight(1f))
    Button(
        onClick = onSave,
        enabled = !state.isLoading && !state.isPhrase && !state.isError,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Lưu")
    }
}

@Composable
private fun MeaningChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun basicMeanings(
    meanings: List<DictionaryMeaning>
): List<Pair<PartOfSpeech, List<String>>> =
    meanings
        .groupBy { it.partOfSpeech }
        .entries
        .take(MAX_POS_GROUPS)
        .map { (pos, list) ->
            pos to list.take(MAX_PER_GROUP)
                .map { cleanMeaning(it.definition).trimEnd('.', ' ') }
        }