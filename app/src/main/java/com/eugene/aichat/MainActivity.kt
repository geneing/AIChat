package com.eugene.aichat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.eugene.aichat.core.ui.theme.LocalThemeController
import com.eugene.aichat.core.ui.theme.ThemeController
import com.eugene.aichat.nav.AppNavHost
import com.eugene.aichat.ui.theme.AIChatTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeController: ThemeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalThemeController provides themeController) {
                AIChatTheme(controller = themeController) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavHost()
                    }
                }
            }
        }
    }
}
