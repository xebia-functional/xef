package com.xebia.functional.xef.auto.llm.openai

sealed class OpenAIHost {
  object OpenAI : OpenAIHost()
  data class Azure(val resourceName: String, val deploymentId: String, val apiVersion: String) : OpenAIHost()
}
