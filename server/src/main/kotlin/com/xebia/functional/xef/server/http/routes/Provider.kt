package com.xebia.functional.xef.server.http.routes

enum class Provider {
    OPENAI, GPT4ALL, GCP
}

fun String.toProvider(): Provider = when (this) {
    "openai" -> Provider.OPENAI
    "gpt4all" -> Provider.GPT4ALL
    "gcp" -> Provider.GCP
    else -> Provider.OPENAI
}