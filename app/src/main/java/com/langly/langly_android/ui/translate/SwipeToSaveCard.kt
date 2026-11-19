package com.langly.langly_android.ui.translate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

internal class SwipeState(val scope: CoroutineScope) {
    val offset = Animatable(0f)
}

@Composable
internal fun rememberSwipeState(): SwipeState {
    val scope = rememberCoroutineScope()
    return remember { SwipeState(scope) }
}

@Composable
internal fun Modifier.swipeToSave(
    state: SwipeState,
    enabled: Boolean,
    canSave: Boolean,
    thresholdPx: Float,
    flyPx: Float,
    onSave: () -> Unit,
    onDismiss: () -> Unit
): Modifier {
    val currentCanSave by rememberUpdatedState(canSave)
    val currentOnSave by rememberUpdatedState(onSave)
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    return this
        .pointerInput(enabled, thresholdPx, flyPx) {
            if (!enabled) return@pointerInput
            detectHorizontalDragGestures(
                onDragStart = { state.scope.launch { state.offset.stop() } },
                onDragEnd = {
                    state.scope.launch {
                        state.settle(
                            canSave = currentCanSave,
                            thresholdPx = thresholdPx,
                            flyPx = flyPx,
                            onSave = currentOnSave,
                            onDismiss = currentOnDismiss
                        )
                    }
                },
                onDragCancel = {
                    state.scope.launch { state.offset.animateTo(0f, tween(200)) }
                },
                onHorizontalDrag = { _, dx ->
                    state.scope.launch { state.offset.snapTo(state.offset.value + dx) }
                }
            )
        }
        .graphicsLayer { translationX = state.offset.value }
}

private suspend fun SwipeState.settle(
    canSave: Boolean,
    thresholdPx: Float,
    flyPx: Float,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val x = offset.value
    when {
        x >= thresholdPx && canSave -> {
            offset.animateTo(flyPx, tween(180))
            onSave()
        }
        x <= -thresholdPx -> {
            offset.animateTo(-flyPx, tween(180))
            onDismiss()
        }
        else -> offset.animateTo(0f, tween(200))
    }
}

@Composable
internal fun BoxScope.SwipeHint(state: SwipeState, thresholdPx: Float, canSave: Boolean) {
    val x = state.offset.value
    val saving = x > 0f
    if (x == 0f || (saving && !canSave)) return
    val hintAlpha = (abs(x) / thresholdPx).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .align(Alignment.Center)
            .graphicsLayer { this.alpha = hintAlpha }
    ) {
        Text(
            text = if (saving) "♥ Lưu" else "‹ Quay về",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}