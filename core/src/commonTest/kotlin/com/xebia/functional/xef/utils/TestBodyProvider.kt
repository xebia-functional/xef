package com.xebia.functional.xef.utils

import com.xebia.functional.openai.infrastructure.BodyProvider
import io.ktor.util.reflect.*

data class TestBodyProvider<T : Any>(val value: T) : BodyProvider<T> {
  override suspend fun body(response: io.ktor.client.statement.HttpResponse): T = value

  @Suppress("UNCHECKED_CAST")
  override suspend fun <V : Any> typedBody(
    response: io.ktor.client.statement.HttpResponse,
    type: TypeInfo
  ): V = value as V
}
