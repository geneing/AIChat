package com.github.geneing.aichat.core.ui.theme

import app.cash.turbine.test
import com.github.geneing.aichat.core.data.prefs.UserPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeControllerImplTest {

    @Test
    fun `default mode is SYSTEM when preferences are empty`() = runTest(UnconfinedTestDispatcher()) {
        val scope = TestScope(testScheduler)
        val themeFlow = MutableStateFlow("SYSTEM")
        val prefs = mockk<UserPreferences>(relaxed = true) {
            every { themeMode } returns themeFlow
        }
        val controller = ThemeControllerImpl(prefs, scope)

        controller.mode.test {
            assertThat(awaitItem()).isEqualTo(ThemeMode.SYSTEM)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setMode writes the new value to preferences`() = runTest(UnconfinedTestDispatcher()) {
        val scope = TestScope(testScheduler)
        val themeFlow = MutableStateFlow("SYSTEM")
        val prefs = mockk<UserPreferences>(relaxed = true) {
            every { themeMode } returns themeFlow
            coEvery { setThemeMode(any()) } coAnswers {
                themeFlow.value = firstArg()
            }
        }
        val controller = ThemeControllerImpl(prefs, scope)

        controller.setMode(ThemeMode.DARK)
        testScheduler.advanceUntilIdle()
        coVerify { prefs.setThemeMode("DARK") }
    }
}
