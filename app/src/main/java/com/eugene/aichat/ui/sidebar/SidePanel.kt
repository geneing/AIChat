package com.eugene.aichat.ui.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.eugene.aichat.R
import com.eugene.aichat.nav.ChatRoute
import com.eugene.aichat.nav.HomeRoute
import com.eugene.aichat.nav.SettingsRoute
import java.text.DateFormat
import java.util.Date

@Composable
fun SidePanel(
    navController: NavHostController,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 12.dp)
        )
        FilledTonalButton(
            onClick = {
                navController.navigate(HomeRoute) {
                    popUpTo(HomeRoute) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Text(
                text = stringResource(R.string.menu_new_chat),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.menu_settings)) },
            selected = false,
            onClick = { navController.navigate(SettingsRoute) },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) }
        )

        if (state.enabledAgents.isNotEmpty()) {
            Text(
                text = "Agents",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            state.enabledAgents.forEach { agent ->
                Text(
                    text = agent.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = "Recents",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
        )
        if (state.chats.isEmpty()) {
            Text(
                text = "No chats yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(state.chats, key = { it.id }) { chat ->
                    NavigationDrawerItem(
                        label = {
                            Column {
                                Text(
                                    text = chat.title.ifBlank { "Untitled" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = DateFormat.getDateTimeInstance(
                                        DateFormat.SHORT, DateFormat.SHORT
                                    ).format(Date(chat.updatedAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selected = false,
                        onClick = {
                            navController.navigate(ChatRoute(chatId = chat.id)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
