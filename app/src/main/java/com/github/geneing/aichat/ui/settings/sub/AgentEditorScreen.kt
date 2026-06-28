package com.github.geneing.aichat.ui.settings.sub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.github.geneing.aichat.R
import com.github.geneing.aichat.ui.settings.AgentEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentEditorScreen(
    agentId: String?,
    navController: NavHostController,
    viewModel: AgentEditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.agent_add)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text(stringResource(R.string.agent_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text(stringResource(R.string.agent_description)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.systemPrompt,
                onValueChange = viewModel::setSystemPrompt,
                label = { Text(stringResource(R.string.agent_system_prompt)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                minLines = 3
            )

            if (state.availableModels.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.agent_model),
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.availableModels.forEach { model ->
                        FilterChip(
                            selected = state.modelConfigId == model.id,
                            onClick = { viewModel.setModelConfigId(model.id) },
                            label = { Text(model.displayName) }
                        )
                    }
                }
            }

            if (state.availableSkills.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.agent_skills),
                    style = MaterialTheme.typography.titleSmall
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.availableSkills.forEach { skill ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = skill.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            FilterChip(
                                selected = skill.id in state.skillIds,
                                onClick = { viewModel.toggleSkill(skill.id) },
                                label = {
                                    Text(if (skill.id in state.skillIds) "On" else "Off")
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.toolAllowListCsv,
                onValueChange = viewModel::setToolAllowListCsv,
                label = { Text(stringResource(R.string.agent_tools)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Max steps: ${state.maxSteps}",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = state.maxSteps.toFloat(),
                onValueChange = { viewModel.setMaxSteps(it.toInt()) },
                valueRange = 1f..12f,
                steps = 10
            )

            state.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.save() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_save))
            }
        }
    }
}
