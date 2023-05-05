package com.xebia.functional.auto

import io.github.oshai.KotlinLogging
import kotlinx.serialization.json.Json

@PublishedApi
internal val logger = KotlinLogging.logger("AutoAI")

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}


