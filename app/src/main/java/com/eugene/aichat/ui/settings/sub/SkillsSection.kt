package com.eugene.aichat.ui.settings.sub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.eugene.aichat.R
import com.eugene.aichat.nav.SkillEditorRoute
import com.eugene.aichat.ui.settings.SettingsViewModel

@Composable
fun SkillsSection(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Text(
        text = stringResource(R.string.settings_section_skills),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    if (state.skills.isEmpty()) {
        Text(
            text = "No skills yet. Built-in skills ship on first launch.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.skills, key = { it.id }) { skill ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(skill.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = skill.description.ifBlank { "(no description)" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = skill.isEnabled,
                            onCheckedChange = { viewModel.setSkillEnabled(skill.id, it) }
                        )
                        if (!skill.isBuiltIn) {
                            IconButton(onClick = { viewModel.deleteSkill(skill.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.common_delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        IconButton(onClick = {
                            navController.navigate(SkillEditorRoute(skillId = skill.id))
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.common_save)
                            )
                        }
                    }
                }
            }
        }
    }
    FilledTonalButton(
        onClick = { navController.navigate(SkillEditorRoute(skillId = null)) }
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null)
        Text(stringResource(R.string.skill_add), modifier = Modifier.padding(start = 8.dp))
    }
}
