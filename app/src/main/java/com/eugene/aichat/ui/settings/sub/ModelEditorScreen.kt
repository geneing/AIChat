package com.eugene.aichat.ui.settings.sub

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.eugene.aichat.R
import com.eugene.aichat.core.domain.model.ProviderType
import com.eugene.aichat.ui.settings.ModelEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelEditorScreen(
    modelId: String?,
    navController: NavHostController,
    viewModel: ModelEditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isEditing) R.string.model_save else R.string.model_add)) },
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
            // Provider segmented
            val providers = listOf(ProviderType.OPENAI, ProviderType.OPENROUTER, ProviderType.OPENCODE)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                providers.forEachIndexed { index, provider ->
                    SegmentedButton(
                        selected = state.providerType == provider,
                        onClick = { viewModel.setProviderType(provider) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = providers.size
                        )
                    ) { Text(provider.name) }
                }
            }

            OutlinedTextField(
                value = state.displayName,
                onValueChange = viewModel::setDisplayName,
                label = { Text(stringResource(R.string.model_display_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = viewModel::setBaseUrl,
                label = { Text(stringResource(R.string.model_base_url)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.model,
                onValueChange = viewModel::setModel,
                label = { Text(stringResource(R.string.model_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.apiKey,
                onValueChange = viewModel::setApiKey,
                label = { Text(stringResource(R.string.model_api_key)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Temperature: ${"%.2f".format(state.temperature)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = state.temperature,
                onValueChange = viewModel::setTemperature,
                valueRange = 0f..2f
            )

            Text(
                text = "Max tokens: ${state.maxTokens}",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = state.maxTokens.toFloat(),
                onValueChange = { viewModel.setMaxTokens(it.toInt()) },
                valueRange = 256f..32_000f,
                steps = 0
            )

            ToggleRow(
                label = "Supports tools",
                value = state.supportsTools,
                onChange = viewModel::setSupportsTools
            )
            ToggleRow(
                label = "Supports vision (images)",
                value = state.supportsVision,
                onChange = viewModel::setSupportsVision
            )
            ToggleRow(
                label = "Supports audio",
                value = state.supportsAudio,
                onChange = viewModel::setSupportsAudio
            )
            ToggleRow(
                label = stringResource(R.string.model_set_default),
                value = state.isDefault,
                onChange = viewModel::setDefault
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
                Text(stringResource(R.string.model_save))
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = value, onCheckedChange = onChange)
    }
}
