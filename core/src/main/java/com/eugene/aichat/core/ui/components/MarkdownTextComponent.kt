package com.eugene.aichat.core.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Renders text via the in-house markdown engine and wires up URL
 * clicks via the [LocalUriHandler]. Use [onUrlClick] to override the
 * default behavior (e.g. for analytics or in-app routing).
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = LocalTextStyle.current,
    onUrlClick: ((String) -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val codeBg = MaterialTheme.colorScheme.surfaceVariant
    val codeFg = MaterialTheme.colorScheme.onSurfaceVariant
    val linkColor = MaterialTheme.colorScheme.primary
    val codeSize: TextUnit = 13.sp

    val annotated: AnnotatedString = remember(text, color, codeBg, codeFg, linkColor) {
        MarkdownRenderer.render(
            text = text,
            linkColor = linkColor,
            codeBackground = codeBg,
            codeText = codeFg,
            codeSize = codeSize
        )
    }

    ClickableText(
        text = annotated,
        modifier = modifier,
        style = style.copy(color = color),
        onClick = { offset ->
            annotated.getStringAnnotations(start = offset, end = offset).forEach { annotation ->
                if (annotation.tag.startsWith("url_")) {
                    val url = annotation.item
                    if (onUrlClick != null) onUrlClick(url) else uriHandler.openUri(url)
                }
            }
        }
    )
}
