package com.eugene.aichat

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.eugene.aichat.core.ui.theme.LocalThemeController
import com.eugene.aichat.core.ui.theme.ThemeController
import com.eugene.aichat.nav.AppNavHost
import com.eugene.aichat.ui.sidebar.SidePanel
import com.eugene.aichat.ui.theme.AIChatTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

val LocalDrawerController = compositionLocalOf<DrawerController> {
    error("DrawerController not provided.")
}

class DrawerController(val open: () -> Unit, val close: () -> Unit)

@AndroidEntryPoint
class MainActivity : androidx.activity.ComponentActivity() {

    @Inject
    lateinit var themeController: ThemeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalThemeController provides themeController) {
                AIChatTheme(controller = themeController) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        RootContent()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootContent() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val controller = remember {
        DrawerController(
            open = { scope.launch { drawerState.open() } },
            close = { scope.launch { drawerState.close() } }
        )
    }
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidePanel(
                navController = navController,
                expanded = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    ) {
        CompositionLocalProvider(LocalDrawerController provides controller) {
            AppNavHost(navController = navController)
        }
    }
}
