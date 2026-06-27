package com.eugene.aichat.core.util

import kotlinx.coroutines.CoroutineDispatcher

data class CoroutineDispatchers(
    val main: CoroutineDispatcher,
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher
)
