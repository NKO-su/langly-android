package com.langly.langly_android.ui.translate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.langly.langly_android.dictionary.DictionaryMeaning

@Composable
internal fun FullContent(
    state: WordPopupState,
    handleDrag: Modifier,
    onCollapse: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    val groups = remember(state.meanings) { state.meanings.groupBy { it.partOfSpeech } }
    val phonetic = state.meanings.firstOrNull()?.phonetic

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        GrabHandle(onClick = onCollapse, dragModifier = handleDrag)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.entryWord,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClose) { Text("Đóng") }
            }

            if (!phonetic.isNullOrBlank()) {
                Text(
                    text = phonetic,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.savedCount > 0) {
                Text(
                    text = "Đã lưu ${state.savedCount} lần",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            groups.forEach { (pos, list) ->
                Text(
                    text = pos.label(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                list.forEach { MeaningItem(it) }
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            Text("Lưu")
        }
    }
}

@Composable
private fun MeaningItem(meaning: DictionaryMeaning) {
    val example = meaning.example.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = cleanMeaning(meaning.definition),
            style = MaterialTheme.typography.bodyLarge
        )
        if (example.isNotBlank()) {
            Text(
                text = example,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}