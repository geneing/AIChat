package com.github.geneing.aichat.core.data.serialization

import kotlinx.serialization.json.Json

val AppJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
    explicitNulls = false
    prettyPrint = false
}
