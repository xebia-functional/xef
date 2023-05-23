package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.csv.RFC4180Parser
import com.xebia.functional.xef.textsplitters.TextSplitter
import com.xebia.functional.xef.textsplitters.TokenTextSplitter
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.utils.io.core.use

suspend fun csv(
  url: String,
  hasHeader: Boolean = true,
  splitter: TextSplitter =
    TokenTextSplitter(ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50),
  configHttp: HttpClientConfig<*>.() -> Unit = {},
  configureRequest: HttpRequestBuilder.() -> Unit = {}
): List<String> =
  HttpClient(configHttp).use {
    val response = it.get(url, configureRequest)
    val parser = RFC4180Parser()
    var headers: List<String> = emptyList()
    val documents =
      response
        .readBytes()
        .decodeToString()
        .lineSequence()
        .mapIndexed { index, row ->
          when {
            hasHeader && index == 0 -> {
              headers = parser.parseLine(row)
              emptyList()
            }
            hasHeader -> headers.zip(parser.parseLine(row)) { key, value -> "$key: $value" }
            else -> parser.parseLine(row)
          }
        }
        .map { row -> row.joinToString() }
        .toList()
    splitter.splitDocuments(documents)
  }
