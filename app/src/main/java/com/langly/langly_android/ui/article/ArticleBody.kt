package com.langly.langly_android.ui.article

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SELECTION_SETTLE_MS = 600L

private const val CLIPBOARD_WAIT_MS = 60L

@Composable
fun ArticleBody(
    html: String,
    onTextSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val linkColor = MaterialTheme.colorScheme.primary

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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnTextSelected by rememberUpdatedState(onTextSelected)
    val toolbar = remember(context, scope) {
        SelectionCaptureToolbar(scope, context) { currentOnTextSelected(it) }
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    CompositionLocalProvider(LocalTextToolbar provides toolbar) {
        SelectionContainer {
            Text(
                text = styled,
                style = MaterialTheme.typography.bodyLarge,
                onTextLayout = { layout = it },
                modifier = modifier.pointerInput(styled) {
                    detectTapGestures { position ->
                        val l = layout ?: return@detectTapGestures
                        if (position.x > l.size.width || position.y > l.size.height) {
                            return@detectTapGestures
                        }
                        val offset = l.getOffsetForPosition(position)
                        val range = l.getWordBoundary(offset)
                        if (range.collapsed) return@detectTapGestures
                        currentOnTextSelected(styled.text.substring(range.start, range.end))
                    }
                }
            )
        }
    }
}

private class SelectionCaptureToolbar(
    private val scope: CoroutineScope,
    private val context: Context,
    private val onSelected: (String) -> Unit
) : TextToolbar {

    private var pending: Job? = null
    private var current = TextToolbarStatus.Hidden

    override val status: TextToolbarStatus get() = current

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        pending?.cancel()
        if (onCopyRequested == null) {
            current = TextToolbarStatus.Hidden
            return
        }
        current = TextToolbarStatus.Shown
        pending = scope.launch {
            delay(SELECTION_SETTLE_MS)
            pending = null
            onCopyRequested()
            delay(CLIPBOARD_WAIT_MS)
            readClipboardText(context)?.let(onSelected)
        }
    }

    override fun hide() {
        pending?.cancel()
        pending = null
        current = TextToolbarStatus.Hidden
    }
}

private fun readClipboardText(context: Context): String? {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = clipboard.primaryClip ?: return null
    if (clip.itemCount == 0) return null
    return clip.getItemAt(0).coerceToText(context)?.toString()
}