package com.eugene.aichat.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.eugene.aichat.R
import com.eugene.aichat.core.domain.model.Message
import com.eugene.aichat.core.domain.model.Role
import com.eugene.aichat.core.ui.components.MarkdownText
import com.eugene.aichat.ui.theme.Dimens

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == Role.USER
    val bg = if (isUser) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    val align = if (isUser) Alignment.End else Alignment.Start
    val bubbleShape = if (isUser) {
        RoundedCornerShape(
            topStart = Dimens.BubbleCornerLarge,
            topEnd = Dimens.BubbleCornerLarge,
            bottomStart = Dimens.BubbleCornerLarge,
            bottomEnd = Dimens.BubbleCornerSmall
        )
    } else {
        RoundedCornerShape(
            topStart = Dimens.BubbleCornerLarge,
            topEnd = Dimens.BubbleCornerLarge,
            bottomStart = Dimens.BubbleCornerSmall,
            bottomEnd = Dimens.BubbleCornerLarge
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = Dimens.ScreenPadding),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = align,
            modifier = Modifier.widthIn(max = Dimens.ContentMaxWidth)
        ) {
            if (!isUser && !message.thinkingText.isNullOrBlank()) {
                ThinkingBlock(
                    thinking = message.thinkingText.orEmpty(),
                    isStreaming = message.isStreaming
                )
            }

            Surface(
                color = bg,
                shape = bubbleShape,
                modifier = Modifier
            ) {
                val body = message.contentText.orEmpty().ifBlank { "…" }
                if (isUser) {
                    Text(
                        text = body,
                        color = fg,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                } else {
                    MarkdownText(
                        text = body,
                        color = fg,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            if (message.isStreaming) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, end = 6.dp)
                        .size(8.dp)
                        .background(fg.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                )
            }

            if (!isUser && message.sources.isNotEmpty()) {
                SourceList(sources = message.sources)
            }
        }
    }
}

@Composable
private fun ThinkingBlock(
    thinking: String,
    isStreaming: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .padding(bottom = 4.dp, end = 4.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isStreaming) {
                    CircularProgressIndicator(
                        strokeWidth = 1.5.dp,
                        modifier = Modifier.size(12.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = stringResource(
                        if (isStreaming) R.string.chat_thinking
                        else if (expanded) R.string.chat_hide_thinking
                        else R.string.chat_show_thinking
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = thinking,
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SourceList(sources: List<com.eugene.aichat.core.domain.model.SourceRef>) {
    Row(
        modifier = Modifier
            .padding(top = 4.dp, end = 4.dp)
            .widthIn(max = Dimens.ContentMaxWidth),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        sources.take(5).forEach { ref ->
            SourceLinkChip(title = ref.title, url = ref.url)
        }
    }
}

@Composable
private fun SourceLinkChip(title: String?, url: String) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { runCatching { uriHandler.openUri(url) } }
    ) {
        Text(
            text = title?.take(20) ?: url.take(20),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
