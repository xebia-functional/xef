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

  override suspend fun <A> promptSpan(prompt: Prompt, block: suspend Metric.() -> A): A =
    state.span("Prompt: ${prompt.messages.lastOrNull()?.contentAsString() ?: "empty"}") {
      with(it) { setAttribute("last-message", prompt.messages.lastOrNull()?.contentAsString() ?: "empty") }
      block()
    }

  override fun event(message: String) {
    state.event(message)
  }

  override fun parameter(key: String, value: String) {
    state.setAttribute(key, value)
  }

  private fun getTracer(scopeName: String? = null): Tracer =
    openTelemetry.getTracer(scopeName ?: config.defaultScopeName)
}
