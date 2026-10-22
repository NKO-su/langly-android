package YOUR_PACKAGE.ui.articles

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun ArticleBody(
    html: String,
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

    SelectionContainer {
        Text(
            text = styled,
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier
        )
    }
}