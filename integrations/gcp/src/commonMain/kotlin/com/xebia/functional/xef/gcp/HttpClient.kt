package com.xebia.functional.xef.gcp

import com.xebia.functional.xef.auto.AutoClose
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * default [HttpClient] to access GCP models using JSON
 */
internal fun AutoClose.jsonHttpClient(): HttpClient = HttpClient {
    install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 60_000
    }
    install(HttpRequestRetry)
    install(ContentNegotiation) {
        json(
            Json {
                encodeDefaults = false
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }
}.let(::autoClose)