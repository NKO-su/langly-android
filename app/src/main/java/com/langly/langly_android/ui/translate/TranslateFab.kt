package com.langly.langly_android.ui.translate

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpDp
import androidx.compose.ui.unit.sp
import com.langly.langly_android.dictionary.DictionaryMeaning
import com.langly.langly_android.dictionary.PartOfSpeech
private const val MAX_POS_GROUPS = 2
private const val MAX_PER_GROUP = 2

private val BUTTON_SIZE = 56.dp
private val SCREEN_MARGIN = 16.dp
private val CARD_HEIGHT = 300.dp

private enum class FabMode { BUTTON, CARD }

private class StateHolder {
    var value: WordPopupState? = null
}

@Composable
fun BoxScope.TranslateFab(viewModel: WordPopupViewModel) {
    val state by viewModel.state.collectAsState()
    val isOpen by viewModel.isCardOpen.collectAsState()
    val context = LocalContext.current

    BackHandler(enabled = isOpen) { viewModel.dismiss() }

    val holder = remember { StateHolder() }
    if (isOpen) holder.value = state
    val shownState = holder.value
    val mode = if (isOpen) FabMode.CARD else FabMode.BUTTON
    val transition = updateTransition(targetState = mode, label = "fab-mode")
    val progress by transition.animateFloat(
        transitionSpec = { tween(300) },
        label = "fab-progress"
    ) { m -> if (m == FabMode.CARD) 1f else 0f }

    val cardWidth = LocalConfiguration.current.screenWidthDp.dp - SCREEN_MARGIN * 2
    val buttonColor = MaterialTheme.colorScheme.primaryContainer
    val cardColor = MaterialTheme.colorScheme.surface

    Surface(
        shape = RoundedCornerShape(lerpDp(16.dp, 24.dp, progress)),
        color = lerpColor(buttonColor, cardColor, progress),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .imePadding()
            .padding(SCREEN_MARGIN)
            .size(
                width = lerpDp(BUTTON_SIZE, cardWidth, progress),
                height = lerpDp(BUTTON_SIZE, CARD_HEIGHT, progress)
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = ((progress - 0.4f) / 0.6f).coerceIn(0f, 1f) }
                        .measureAtSize(cardWidth, CARD_HEIGHT)
                ) {
                    TranslateCardContent(
                        state = shownState,
                        onSubmit = viewModel::onTextSelected,
                        onSave = {
                            val current = state
                            if (current != null) {
                                viewModel.saveCurrent()
                                val msg = if (current.savedCount > 0) "Đã lưu thêm \"${current.entryWord}\""
                                else "Đã lưu \"${current.entryWord}\""
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClose = viewModel::dismiss
                    )
                }
            }
            if (progress < 1f) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = (1f - progress / 0.4f).coerceIn(0f, 1f) }
                ) {
                    FabButtonContent(onClick = viewModel::openCard)
                }
            }
        }
    }
}
private fun Modifier.measureAtSize(width: Dp, height: Dp): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(
            Constraints.fixed(width.roundToPx(), height.roundToPx())
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(0, 0)
        }
    }

@Composable
private fun FabButtonContent(onClick: () -> Unit) {
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    Column(
        modifier = Modifier
            .size(BUTTON_SIZE)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "文A",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Text(
            text = "Dịch",
            fontSize = 11.sp,
            color = contentColor
        )
    }
}

@Composable
private fun TranslateCardContent(
    state: WordPopupState?,
    onSubmit: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state?.entryWord ?: "Dịch",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onClose) { Text("Đóng") }
        }

        if (state == null) {
            InputSection(onSubmit = onSubmit)
        } else {
            ResultSection(state = state, onSave = onSave)
        }
    }
}

@Composable
private fun InputSection(onSubmit: (String) -> Unit) {
    var input by remember { mutableStateOf("") }

    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text("Nhập từ hoặc cụm từ") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit(input) }),
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = { onSubmit(input) },
        enabled = input.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Tra")
    }
    Text(
        text = "Hoặc nhấn vào một từ trong bài để tra.",
        style = MaterialTheme.typography.bodySmall
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.ResultSection(
    state: WordPopupState,
    onSave: () -> Unit
) {
    val phonetic = state.meanings.firstOrNull()?.phonetic

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

private fun PartOfSpeech.label(): String = when (this) {
    PartOfSpeech.NOUN -> "danh từ"
    PartOfSpeech.VERB -> "động từ"
    PartOfSpeech.ADJECTIVE -> "tính từ"
    PartOfSpeech.OTHER -> "khác"
}