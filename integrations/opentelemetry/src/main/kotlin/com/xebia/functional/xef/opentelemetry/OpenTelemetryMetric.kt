package com.xebia.functional.xef.opentelemetry

import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.prompt.Prompt
import io.opentelemetry.api.trace.*

class OpenTelemetryMetric(
  private val config: OpenTelemetryConfig = OpenTelemetryConfig.create("xef", "io.xef")
) : Metric {

  private val openTelemetry = config.newInstance()

  private val state = OpenTelemetryState(getTracer())

  override suspend fun <A> customSpan(name: String, block: suspend Metric.() -> A): A =
    state.span(name) { block() }

  override suspend fun <A, T> promptSpan(prompt: Prompt<T>, block: suspend Metric.() -> A): A =
    state.span("Prompt: ${prompt.messages.lastOrNull()?.contentAsString() ?: "empty"}") { span ->
      span.setAttribute("last-message", prompt.messages.lastOrNull()?.contentAsString() ?: "empty")
      block()
    }

  override suspend fun event(message: String) {
    state.event(message)
  }

  override suspend fun parameter(key: String, value: String) {
    state.setAttribute(key, value)
  }

  private fun getTracer(scopeName: String? = null): Tracer =
    openTelemetry.getTracer(scopeName ?: config.defaultScopeName)
}
