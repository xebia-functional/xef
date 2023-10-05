package com.xebia.functional.xef.server.models.mlflow

val filterOutFields: List<String> = listOf(
    "model",
    "top_p",
    "stream",
    "presence_penalty",
    "frequency_penalty",
    "logit_bias",
    "user"
)

val mappedFields: Map<String, String> = mapOf(
    "n" to "candidate_count"
)