package com.langly.langly_android.ui.article

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun ArticleBody(
    html: String,
    onTextSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val haptic = LocalHapticFeedback.current

    val styled = remember(html, linkColor) {
        AnnotatedString.fromHtml(
            htmlString = html,
            linkStyles = TextLinkStyles(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline
                )
            )
        )
    }

    val currentOnTextSelected by rememberUpdatedState(onTextSelected)
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var selection by remember(html) { mutableStateOf<TextRange?>(null) }

    Text(
        text = styled,
        style = MaterialTheme.typography.bodyLarge,
        onTextLayout = { layout = it },
        modifier = modifier
            .drawBehind {
                val l = layout
                val s = selection
                if (l != null && s != null) {
                    drawPath(l.getPathForRange(s.start, s.end), highlightColor)
                }
            }
            .pointerInput(styled) {
                detectTapGestures { position ->
                    val l = layout ?: return@detectTapGestures
                    if (position.x > l.size.width || position.y > l.size.height) {
                        return@detectTapGestures
                    }
                    val range = wordRangeAt(l, position)
                    val word = styled.text.substring(range.start, range.end)
                    if (word.isBlank()) {
                        selection = null
                        return@detectTapGestures
                    }
                    selection = range
                    currentOnTextSelected(word)
                }
            }
            .pointerInput(styled) {
                var anchor: TextRange? = null
                var before: TextRange? = null

                detectDragGesturesAfterLongPress(
                    onDragStart = { position ->
                        val l = layout ?: return@detectDragGesturesAfterLongPress
                        val r = wordRangeAt(l, position)
                        before = selection
                        anchor = r
                        selection = r
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { change, _ ->
                        val l = layout ?: return@detectDragGesturesAfterLongPress
                        val a = anchor ?: return@detectDragGesturesAfterLongPress
                        selection = unionOf(a, wordRangeAt(l, change.position))
                    },
                    onDragEnd = {
                        val a = anchor
                        val sel = selection
                        anchor = null
                        if (a != null && sel != null && sel != a) {
                            val text = styled.text.substring(sel.start, sel.end).trim()
                            if (text.isNotEmpty()) currentOnTextSelected(text)
                        } else {
                            selection = before
                        }
                    },
                    onDragCancel = {
                        anchor = null
                        selection = before
                    }
                )
            }
    )
}
private fun wordRangeAt(layout: TextLayoutResult, position: Offset): TextRange {
    val x = position.x.coerceIn(0f, layout.size.width.toFloat())
    val y = position.y.coerceIn(0f, (layout.size.height - 1).toFloat().coerceAtLeast(0f))
    val offset = layout.getOffsetForPosition(Offset(x, y))
    return layout.getWordBoundary(offset)
}
private fun unionOf(a: TextRange, b: TextRange): TextRange =
    TextRange(minOf(a.start, b.start), maxOf(a.end, b.end))