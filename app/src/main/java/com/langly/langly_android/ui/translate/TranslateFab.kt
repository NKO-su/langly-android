package com.langly.langly_android.ui.translate

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpDp
import kotlinx.coroutines.launch

private val SCREEN_MARGIN = 16.dp
private val CARD_HEIGHT = 360.dp
private val SWIPE_THRESHOLD = 96.dp

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
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var isFull by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(1f) }
    var dragOrigin by remember { mutableFloatStateOf(1f) }
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val swipe = rememberSwipeState()

    BackHandler(enabled = isOpen) { viewModel.dismiss() }
    BackHandler(enabled = isFull) { isFull = false }

    val holder = remember { StateHolder() }
    if (isOpen) holder.value = state
    val shownState = holder.value

    val canExpand = shownState?.hasResult() == true
    val canSave = shownState != null &&
            !shownState.isLoading && !shownState.isPhrase && !shownState.isError

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

    val target = when (mode) {
        FabMode.BUTTON -> 0f
        FabMode.CARD -> 1f
        FabMode.FULL -> 2f
    }

    LaunchedEffect(target, dragging) {
        if (!dragging) progress.animateTo(target, tween(300))
    }

    LaunchedEffect(Unit) {
        snapshotFlow { progress.value }.collect {
            if (it == 0f) swipe.offset.snapTo(0f)
        }
    }

    val p = progress.value

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

        val toCard = p.coerceIn(0f, 1f)
        val toFull = (p - 1f).coerceIn(0f, 1f)
        val bottomBase = maxOf(bottomPadding, imeBottom) + SCREEN_MARGIN

        val shellWidth = lerpDp(lerpDp(BUTTON_SIZE, cardWidth, toCard), fullWidth, toFull)
        val shellHeight = lerpDp(lerpDp(BUTTON_SIZE, CARD_HEIGHT, toCard), fullHeight, toFull)
        val corner = lerpDp(lerpDp(16.dp, 24.dp, toCard), 0.dp, toFull)
        val endMargin = lerpDp(SCREEN_MARGIN, 0.dp, toFull)
        val bottomMargin = lerpDp(bottomBase, 0.dp, toFull)

        val travelPx = with(density) { (fullHeight - bottomBase - CARD_HEIGHT).toPx() }.coerceAtLeast(1f)
        val expandThreshold = with(density) { 50.dp.toPx() } / travelPx
        val collapseThreshold = with(density) { 90.dp.toPx() } / travelPx

        val swipeThresholdPx = with(density) { SWIPE_THRESHOLD.toPx() }
        val swipeFlyPx = with(density) { fullWidth.toPx() }

        val onDragStart: () -> Unit = {
            dragging = true
            dragValue = progress.value.coerceIn(1f, 2f)
            dragOrigin = dragValue
        }
        val onDragDelta: (Float) -> Unit = { dy ->
            dragValue = (dragValue - dy / travelPx).coerceIn(1f, 2f)
            scope.launch { progress.snapTo(dragValue) }
        }
        val onDragEnd: () -> Unit = {
            val moved = dragValue - dragOrigin
            isFull = if (dragOrigin >= 1.5f) moved > -collapseThreshold else moved >= expandThreshold
            dragging = false
        }
        val cardHandleDrag = rememberHandleDrag(
            enabled = canExpand,
            onStart = onDragStart,
            onDelta = onDragDelta,
            onEnd = onDragEnd
        )
        val fullHandleDrag = rememberHandleDrag(
            enabled = true,
            onStart = onDragStart,
            onDelta = onDragDelta,
            onEnd = onDragEnd
        )

        Surface(
            shape = RoundedCornerShape(corner),
            color = lerpColor(buttonColor, cardColor, toCard),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = endMargin, bottom = bottomMargin)
                .size(width = shellWidth, height = shellHeight)
                .swipeToSave(
                    state = swipe,
                    enabled = mode == FabMode.CARD && !dragging,
                    canSave = canSave,
                    thresholdPx = swipeThresholdPx,
                    flyPx = swipeFlyPx,
                    onSave = {
                        save()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.dismiss()
                    },
                    onDismiss = viewModel::dismiss
                )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (p > 0f && p < 2f) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer { alpha = cardAlpha(progress.value) }
                            .measureAtSize(cardWidth, CARD_HEIGHT)
                    ) {
                        TranslateCardContent(
                            state = shownState,
                            canExpand = canExpand,
                            handleDrag = cardHandleDrag,
                            onExpand = { isFull = true },
                            onSubmit = viewModel::onTextTyped,
                            onSave = save,
                            onOpenBase = viewModel::openBaseWord,
                            onClose = viewModel::dismiss
                        )
                    }
                }
                if (p > 1f && shownState != null) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer { alpha = ((progress.value - 1.5f) / 0.5f).coerceIn(0f, 1f) }
                            .measureAtSize(fullWidth, fullHeight)
                    ) {
                        FullContent(
                            state = shownState,
                            handleDrag = fullHandleDrag,
                            onCollapse = { isFull = false },
                            onSave = save,
                            onClose = viewModel::dismiss
                        )
                    }
                }
                if (p < 1f) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer { alpha = (1f - progress.value / 0.4f).coerceIn(0f, 1f) }
                    ) {
                        FabButtonContent(onClick = viewModel::openCard)
                    }
                }
                SwipeHint(state = swipe, thresholdPx = swipeThresholdPx, canSave = canSave)
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