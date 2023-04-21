package com.xebia.functional.llm.huggingface

import arrow.fx.coroutines.ResourceScope
import com.xebia.functional.configure
import com.xebia.functional.env.HuggingFaceConfig
import com.xebia.functional.httpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

interface HuggingFaceClient {
  suspend fun generate(request: InferenceRequest, model: Model): List<Generation>
}

suspend fun ResourceScope.KtorHuggingFaceClient(
  engine: HttpClientEngine,
  config: HuggingFaceConfig
): HuggingFaceClient = KtorHuggingFaceClient(httpClient(engine), config)

private class KtorHuggingFaceClient(
  private val httpClient: HttpClient,
  private val config: HuggingFaceConfig
) : HuggingFaceClient {

  // TODO move to config
  private val baseUrl = "https://api-inference.huggingface.co"

  override suspend fun generate(request: InferenceRequest, model: Model): List<Generation> {
    val response = httpClient.post("$baseUrl/models/${model.name}") {
      configure(config.token, request)
    }
    return response.body()
  }
}
