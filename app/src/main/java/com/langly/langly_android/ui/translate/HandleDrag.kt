package com.langly.langly_android.ui.translate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp

internal val HANDLE_HEIGHT = 28.dp

private class CoordsHolder {
    var coords: LayoutCoordinates? = null
}

@Composable
internal fun rememberHandleDrag(
    enabled: Boolean,
    onStart: () -> Unit,
    onDelta: (Float) -> Unit,
    onEnd: () -> Unit
): Modifier {
    val holder = remember { CoordsHolder() }
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnDelta by rememberUpdatedState(onDelta)
    val currentOnEnd by rememberUpdatedState(onEnd)

    return Modifier
        .onGloballyPositioned { holder.coords = it }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            var lastY = 0f
            detectVerticalDragGestures(
                onDragStart = { offset ->
                    lastY = holder.coords?.localToRoot(offset)?.y ?: 0f
                    currentOnStart()
                },
                onDragEnd = { currentOnEnd() },
                onDragCancel = { currentOnEnd() },
                onVerticalDrag = { change, _ ->
                    val y = holder.coords?.localToRoot(change.position)?.y ?: lastY
                    currentOnDelta(y - lastY)
                    lastY = y
                }
            )
        }
}

@Composable
internal fun GrabHandle(onClick: () -> Unit, dragModifier: Modifier) {
    Box(
        modifier = dragModifier
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