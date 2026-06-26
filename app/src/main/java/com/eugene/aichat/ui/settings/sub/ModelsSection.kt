package com.eugene.aichat.ui.settings.sub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.eugene.aichat.R
import com.eugene.aichat.nav.ModelEditorRoute

@Composable
fun ModelsSection(navController: NavHostController) {
    Text(
        text = stringResource(R.string.settings_section_models),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Button(
        onClick = { navController.navigate(ModelEditorRoute(modelId = null)) },
        modifier = Modifier
    ) {
        Text(stringResource(R.string.model_add))
    }
    Text(
        text = "Add / edit / delete per provider — ships in step 3.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
