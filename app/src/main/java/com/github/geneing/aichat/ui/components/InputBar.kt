package com.github.geneing.aichat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.geneing.aichat.R

@Composable
fun InputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    onVoicePress: () -> Unit,
    onStop: (() -> Unit)? = null,
    isStreaming: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAttach) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.cd_attach_file),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            singleLine = true,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.chat_input_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                inner()
            }
        )

        when {
            isStreaming && onStop != null -> {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Stop,
                        contentDescription = stringResource(R.string.chat_stop),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            value.isBlank() -> {
                IconButton(onClick = onVoicePress) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = stringResource(R.string.chat_voice_mode),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = stringResource(R.string.cd_send_message),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
