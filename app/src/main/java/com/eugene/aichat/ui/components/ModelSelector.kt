package com.eugene.aichat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eugene.aichat.R
import com.eugene.aichat.core.domain.model.ModelConfig

@Composable
fun ModelSelector(
    models: List<ModelConfig>,
    activeModel: ModelConfig?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(enabled = models.isNotEmpty()) { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = activeModel?.displayName ?: "Select model",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = stringResource(R.string.cd_pick_model),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (models.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No models — add one in Settings") },
                    onClick = { expanded = false }
                )
            } else {
                models.forEach { model ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(model.displayName)
                                Text(
                                    text = model.providerType.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onPick(model.id)
                        }
                    )
                }
            }
        }
    }
}
