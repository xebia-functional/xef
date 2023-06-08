package com.xebia.functional.xef.llm.huggingface

import arrow.fx.coroutines.ResourceScope
import com.xebia.functional.xef.configure
import com.xebia.functional.xef.env.HuggingFaceConfig
import com.xebia.functional.xef.httpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.path

interface HuggingFaceClient {
  suspend fun generate(request: InferenceRequest, model: Model): List<Generation>
}

suspend fun ResourceScope.KtorHuggingFaceClient(config: HuggingFaceConfig): HuggingFaceClient =
  KtorHuggingFaceClient(httpClient(config.baseUrl), config)

private class KtorHuggingFaceClient(
  private val httpClient: HttpClient,
  private val config: HuggingFaceConfig
) : HuggingFaceClient {

  override suspend fun generate(request: InferenceRequest, model: Model): List<Generation> {
    val response =
      httpClient.post {
        url { path("models", model.name) }
        configure(config.token, request)
      }
    return response.body()
  }
}
