package com.github.geneing.aichat.ui.chat.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.geneing.aichat.R
import com.github.geneing.aichat.core.voice.VoiceSessionState
import com.github.geneing.aichat.core.voice.VoiceUiState
import com.github.geneing.aichat.ui.theme.SparkBlue
import com.github.geneing.aichat.ui.theme.SparkCyan
import com.github.geneing.aichat.ui.theme.SparkPink
import com.github.geneing.aichat.ui.theme.SparkViolet

@Composable
fun VoiceModeOverlay(
    state: VoiceUiState,
    onClose: () -> Unit,
    onStartListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.voice_exit),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            VoiceOrb(state = state.state)

            Spacer(Modifier.height(16.dp))

            Text(
                text = state.partialTranscript.ifBlank { state.finalTranscript },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = when (state.state) {
                    VoiceSessionState.LISTENING -> stringResource(R.string.voice_listening)
                    VoiceSessionState.THINKING -> stringResource(R.string.voice_thinking)
                    VoiceSessionState.SPEAKING -> stringResource(R.string.voice_speaking)
                    VoiceSessionState.IDLE -> "Tap the mic to start"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            if (state.state == VoiceSessionState.IDLE) {
                IconButton(
                    onClick = onStartListening,
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
                }
            }
        }
    }
}

@Composable
private fun VoiceOrb(state: VoiceSessionState) {
    val infinite = rememberInfiniteTransition(label = "voice-orb")
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = if (state == VoiceSessionState.SPEAKING) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice-pulse"
    )

    Box(
        modifier = Modifier
            .size(180.dp)
            .scale(pulse)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SparkBlue, SparkViolet, SparkPink, SparkCyan)
                ),
                shape = CircleShape
            )
    )
}
