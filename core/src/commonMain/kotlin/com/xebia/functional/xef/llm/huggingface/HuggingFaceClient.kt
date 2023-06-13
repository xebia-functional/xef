package com.xebia.functional.xef.llm.huggingface

import com.xebia.functional.xef.configure
import com.xebia.functional.xef.env.HuggingFaceConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json

interface HuggingFaceClient {
  suspend fun generate(request: InferenceRequest, model: Model): List<Generation>
}

class KtorHuggingFaceClient(private val config: HuggingFaceConfig) :
  HuggingFaceClient, AutoCloseable {

  private val httpClient: HttpClient = HttpClient {
    install(HttpTimeout)
    install(ContentNegotiation) { json() }
    defaultRequest { url(config.baseUrl) }
  }

  override suspend fun generate(request: InferenceRequest, model: Model): List<Generation> {
    val response =
      httpClient.post {
        url { path("models", model.name) }
        configure(config.token, request)
      }
    return response.body()
  }

  override fun close() {
    httpClient.close()
  }
}
