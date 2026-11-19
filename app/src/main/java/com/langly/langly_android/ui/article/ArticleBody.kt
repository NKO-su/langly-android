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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.compose.ui.unit.dp

private val WORD_REGEX = Regex("[\\p{L}\\p{N}]+(?:['’][\\p{L}]+)?")

@Composable
fun ArticleBody(
    html: String,
    onTextSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    savedWords: Set<String> = emptySet()
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val savedColor = MaterialTheme.colorScheme.tertiary
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

    val savedRanges = remember(styled, savedWords) {
        if (savedWords.isEmpty()) {
            emptyList()
        } else {
            WORD_REGEX.findAll(styled.text)
                .filter { it.value.lowercase() in savedWords }
                .map { TextRange(it.range.first, it.range.last + 1) }
                .toList()
        }
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
                if (l != null && savedRanges.isNotEmpty()) {
                    val stroke = 1.5.dp.toPx()
                    val gap = 3.dp.toPx()
                    val effect = PathEffect.dashPathEffect(floatArrayOf(0.1f, 4.dp.toPx()))
                    val textLength = l.layoutInput.text.length
                    savedRanges.forEach { r ->
                        if (r.end > textLength) return@forEach
                        val line = l.getLineForOffset(r.start)
                        if (l.getLineForOffset(r.end - 1) != line) return@forEach
                        val bounds = l.getPathForRange(r.start, r.end).getBounds()
                        val y = l.getLineBaseline(line) + gap
                        drawLine(
                            color = savedColor,
                            start = Offset(bounds.left, y),
                            end = Offset(bounds.right, y),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round,
                            pathEffect = effect
                        )
                    }
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
                        val text = if (a != null && sel != null) {
                            styled.text.substring(sel.start, sel.end).trim()
                        } else ""
                        if (text.isNotEmpty()) {
                            currentOnTextSelected(text)
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