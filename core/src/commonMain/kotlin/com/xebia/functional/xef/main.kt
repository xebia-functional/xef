package com.xebia.functional.xef

import arrow.core.raise.either
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.xef.env.Env
import com.xebia.functional.xef.llm.huggingface.HuggingFaceClient
import com.xebia.functional.xef.llm.huggingface.InferenceRequest
import com.xebia.functional.xef.llm.huggingface.KtorHuggingFaceClient
import com.xebia.functional.xef.llm.huggingface.Model
import com.xebia.functional.xef.llm.openai.CompletionRequest
import com.xebia.functional.xef.llm.openai.EmbeddingRequest
import com.xebia.functional.xef.llm.openai.KtorOpenAIClient
import com.xebia.functional.xef.llm.openai.OpenAIClient

suspend fun main(): Unit = resourceScope {
  either {
      val env = Env()
      val openAPI = KtorOpenAIClient(env.openAI)
      val huggingFace = KtorHuggingFaceClient(env.huggingFace)

      println(openAIExample(openAPI))
      println(hfExample(huggingFace))
      println(openAIEmbeddingsExample(openAPI))
    }
    .onLeft { println(it) }
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
  client.generate(
    InferenceRequest("Write a tagline for an ice cream shop."),
    Model("google/flan-t5-xl")
  )
