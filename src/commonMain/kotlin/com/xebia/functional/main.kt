package com.xebia.functional

import arrow.core.raise.either
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.env.Env
import com.xebia.functional.llm.huggingface.HuggingFaceClient
import com.xebia.functional.llm.huggingface.InferenceRequest
import com.xebia.functional.llm.huggingface.KtorHuggingFaceClient
import com.xebia.functional.llm.huggingface.Model
import com.xebia.functional.llm.openai.CompletionRequest
import com.xebia.functional.llm.openai.EmbeddingRequest
import com.xebia.functional.llm.openai.KtorOpenAIClient
import com.xebia.functional.llm.openai.OpenAIClient
import io.ktor.client.engine.HttpClientEngine

suspend fun main(): Unit = resourceScope {
  either {
    val env = Env()
    val openAPI = KtorOpenAIClient(env.openAI)
    val huggingFace = KtorHuggingFaceClient(env.huggingFace)

    println(openAIExample(openAPI))
    println(hfExample(huggingFace))
    println(openAIEmbeddingsExample(openAPI))
  }.onLeft { println(it) }
}

suspend fun openAIEmbeddingsExample(client: OpenAIClient) =
  client.createEmbeddings(
    EmbeddingRequest(
      model = "text-embedding-ada-002",
      input = listOf("How much is 2+2"),
      user = "testing"
    )
  )

suspend fun openAIExample(client: OpenAIClient) =
  client.createCompletion(
    CompletionRequest(
      model = "ada",
      user = "testing",
      prompt = "Write a tagline for an ice cream shop.",
      echo = true,
      n = 3
    )
  )

suspend fun hfExample(client: HuggingFaceClient) =
  client.generate(InferenceRequest("Write a tagline for an ice cream shop."), Model("google/flan-t5-xl"))
