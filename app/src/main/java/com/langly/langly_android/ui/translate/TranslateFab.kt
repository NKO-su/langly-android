package com.langly.langly_android.ui.translate

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
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
private val CARD_HEIGHT = 360.dp
private val HANDLE_HEIGHT = 28.dp

private enum class FabMode { BUTTON, CARD, FULL }

private class StateHolder {
    var value: WordPopupState? = null
}

private fun WordPopupState.hasResult(): Boolean =
    !isLoading && !isError && !isPhrase && meanings.isNotEmpty()

private fun cardAlpha(progress: Float): Float =
    if (progress <= 1f) ((progress - 0.4f) / 0.6f).coerceIn(0f, 1f)
    else (1f - (progress - 1f) / 0.4f).coerceIn(0f, 1f)

@Composable
fun TranslateFab(viewModel: WordPopupViewModel, bottomPadding: Dp) {
    val state by viewModel.state.collectAsState()
    val isOpen by viewModel.isCardOpen.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current

    var isFull by remember { mutableStateOf(false) }

    BackHandler(enabled = isOpen) { viewModel.dismiss() }
    BackHandler(enabled = isFull) { isFull = false }

    val holder = remember { StateHolder() }
    if (isOpen) holder.value = state
    val shownState = holder.value

    val canExpand = shownState?.hasResult() == true

    LaunchedEffect(isOpen, canExpand) {
        if (!isOpen || !canExpand) isFull = false
    }

    val mode = when {
        !isOpen -> FabMode.BUTTON
        isFull && canExpand -> FabMode.FULL
        else -> FabMode.CARD
    }

    LaunchedEffect(mode) {
        if (mode == FabMode.FULL) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }

    val transition = updateTransition(targetState = mode, label = "fab-mode")
    val progress by transition.animateFloat(
        transitionSpec = { tween(300) },
        label = "fab-progress"
    ) { m ->
        when (m) {
            FabMode.BUTTON -> 0f
            FabMode.CARD -> 1f
            FabMode.FULL -> 2f
        }
    }

    val save: () -> Unit = {
        val current = state
        if (current != null) {
            viewModel.saveCurrent()
            val msg = if (current.savedCount > 0) "Đã lưu thêm \"${current.entryWord}\""
            else "Đã lưu \"${current.entryWord}\""
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val imeBottom = with(density) { WindowInsets.ime.getBottom(density).toDp() }
    val buttonColor = MaterialTheme.colorScheme.primaryContainer
    val cardColor = MaterialTheme.colorScheme.surface

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val fullWidth = maxWidth
        val fullHeight = maxHeight
        val cardWidth = fullWidth - SCREEN_MARGIN * 2

        val toCard = progress.coerceIn(0f, 1f)
        val toFull = (progress - 1f).coerceIn(0f, 1f)

        val shellWidth = lerpDp(lerpDp(BUTTON_SIZE, cardWidth, toCard), fullWidth, toFull)
        val shellHeight = lerpDp(lerpDp(BUTTON_SIZE, CARD_HEIGHT, toCard), fullHeight, toFull)
        val corner = lerpDp(lerpDp(16.dp, 24.dp, toCard), 0.dp, toFull)
        val endMargin = lerpDp(SCREEN_MARGIN, 0.dp, toFull)
        val bottomMargin = lerpDp(maxOf(bottomPadding, imeBottom) + SCREEN_MARGIN, 0.dp, toFull)

        Surface(
            shape = RoundedCornerShape(corner),
            color = lerpColor(buttonColor, cardColor, toCard),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = endMargin, bottom = bottomMargin)
                .size(width = shellWidth, height = shellHeight)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (progress > 0f && progress < 2f) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer { alpha = cardAlpha(progress) }
                            .measureAtSize(cardWidth, CARD_HEIGHT)
                    ) {
                        TranslateCardContent(
                            state = shownState,
                            canExpand = canExpand,
                            onExpand = { isFull = true },
                            onSubmit = viewModel::onTextTyped,
                            onSave = save,
                            onClose = viewModel::dismiss
                        )
                    }
                }
                if (progress > 1f && shownState != null) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer { alpha = ((progress - 1.5f) / 0.5f).coerceIn(0f, 1f) }
                            .measureAtSize(fullWidth, fullHeight)
                    ) {
                        FullContent(
                            state = shownState,
                            onCollapse = { isFull = false },
                            onSave = save,
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
private fun GrabHandle(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HANDLE_HEIGHT)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
private fun TranslateCardContent(
    state: WordPopupState?,
    canExpand: Boolean,
    onExpand: () -> Unit,
    onSubmit: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    val showInput = state == null || state.fromInput

    Column(modifier = Modifier.fillMaxSize()) {
        if (canExpand) {
            GrabHandle(onClick = onExpand)
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
                ResultSection(state = state, showWord = state.fromInput, onSave = onSave)
            }
        }
    }
}

@Composable
private fun FullContent(
    state: WordPopupState,
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
        GrabHandle(onClick = onCollapse)

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
    onSave: () -> Unit
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