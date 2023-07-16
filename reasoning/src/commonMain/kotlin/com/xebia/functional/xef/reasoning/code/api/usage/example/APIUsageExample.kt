package com.xebia.functional.xef.reasoning.code.api.usage.example

import kotlinx.serialization.Serializable

@Serializable
data class APIUsageExample(
  val apiName: String,
  val description: String,
  val codeSnippet: String,
  val exampleResult: String
)
