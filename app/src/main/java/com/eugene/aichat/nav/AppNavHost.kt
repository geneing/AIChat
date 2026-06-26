package com.eugene.aichat.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.eugene.aichat.ui.chat.ChatScreen
import com.eugene.aichat.ui.settings.SettingsScreen
import com.eugene.aichat.ui.settings.sub.AgentEditorScreen
import com.eugene.aichat.ui.settings.sub.ModelEditorScreen
import com.eugene.aichat.ui.settings.sub.SkillEditorScreen
import com.eugene.aichat.ui.sidebar.SidePanel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            ChatScreen(
                chatId = NavArgs.NEW_CHAT,
                agentId = null,
                navController = navController
            )
        }
        composable<ChatRoute> { backStack ->
            val route: ChatRoute = backStack.toRoute()
            ChatScreen(
                chatId = route.chatId,
                agentId = route.agentId,
                navController = navController
            )
        }
        composable<HistoryRoute> {
            SidePanel(navController = navController, expanded = true)
        }
        composable<SettingsRoute> {
            SettingsScreen(navController = navController)
        }
        composable<ModelEditorRoute> { backStack ->
            val route: ModelEditorRoute = backStack.toRoute()
            ModelEditorScreen(modelId = route.modelId, navController = navController)
        }
        composable<SkillEditorRoute> { backStack ->
            val route: SkillEditorRoute = backStack.toRoute()
            SkillEditorScreen(skillId = route.skillId, navController = navController)
        }
        composable<AgentEditorRoute> { backStack ->
            val route: AgentEditorRoute = backStack.toRoute()
            AgentEditorScreen(agentId = route.agentId, navController = navController)
        }
    }
}
