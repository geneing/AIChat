package com.eugene.aichat.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.eugene.aichat.R
import com.eugene.aichat.nav.HistoryRoute
import com.eugene.aichat.nav.SettingsRoute

@Composable
fun SidePanel(
    navController: NavHostController,
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PaddingValues(horizontal = 12.dp, vertical = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 16.dp)
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_new_chat)) },
            selected = false,
            onClick = {
                navController.navigate(com.eugene.aichat.nav.HomeRoute) {
                    popUpTo(HistoryRoute) { inclusive = true }
                }
            },
            icon = { Icon(Icons.Outlined.Add, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_settings)) },
            selected = false,
            onClick = { navController.navigate(SettingsRoute) },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) }
        )
        Text(
            text = "Recent",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Empty for the scaffold; will be populated in step 12.
        }
    }
}
