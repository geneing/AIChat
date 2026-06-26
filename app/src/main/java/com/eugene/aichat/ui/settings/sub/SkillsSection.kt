package com.eugene.aichat.ui.settings.sub

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.eugene.aichat.R
import com.eugene.aichat.nav.SkillEditorRoute

@Composable
fun SkillsSection(navController: NavHostController) {
    Text(
        text = stringResource(R.string.settings_section_skills),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Button(
        onClick = { navController.navigate(SkillEditorRoute(skillId = null)) },
        modifier = Modifier
    ) {
        Text(stringResource(R.string.skill_add))
    }
    Text(
        text = "Skill CRUD ships in step 13.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
