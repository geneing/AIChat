package com.eugene.aichat.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eugene.aichat.core.domain.model.Message
import com.eugene.aichat.core.domain.model.Role
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
        horizontalArrangement = if (isUser) androidx.compose.foundation.layout.Arrangement.End
        else androidx.compose.foundation.layout.Arrangement.Start
    ) {
        Column(
            horizontalAlignment = align,
            modifier = Modifier.widthIn(max = Dimens.ContentMaxWidth)
        ) {
            Box(
                modifier = Modifier
                    .background(bg, bubbleShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.contentText.orEmpty().ifBlank { "…" },
                    color = fg,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (message.isStreaming) {
                StreamingCaret(color = fg)
            }
        }
    }
}

@Composable
private fun StreamingCaret(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .background(color.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
    ) {
        Text(
            text = "▍",
            color = color,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
