package com.xebia.functional.xef.opentelemetry

import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.openapi.MessageObject
import com.xebia.functional.xef.openapi.RunObject
import com.xebia.functional.xef.openapi.RunStepObject
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.contentAsString
import io.opentelemetry.api.trace.*

class OpenTelemetryMetric(
  private val config: OpenTelemetryConfig = OpenTelemetryConfig.create("xef", "io.xef")
) : Metric {

  private val openTelemetry = config.newInstance()

  private val state = OpenTelemetryState(getTracer())

  private val assistantState = OpenTelemetryAssistantState(getTracer())

  override suspend fun <A> customSpan(
    name: String,
    parameters: Map<String, String>,
    block: suspend Metric.() -> A
  ): A = state.span(name, parameters) { block() }

  override suspend fun <A> promptSpan(prompt: Prompt, block: suspend Metric.() -> A): A =
    state.span(
      "Prompt: ${prompt.messages.lastOrNull()?.contentAsString() ?: "empty"}",
      mapOf(
        "prompt" to (prompt.messages.lastOrNull()?.contentAsString() ?: "empty"),
        "functions" to (prompt.functions.joinToString { it.name }),
        "configuration" to prompt.configuration.toString(),
        "model" to prompt.model.toString()
      )
    ) {
      block()
    }

  override suspend fun event(message: String) {
    state.event(message)
  }

  override suspend fun parameter(key: String, value: String) {
    state.setAttribute(key, value)
  }

  override suspend fun parameter(key: String, values: List<String>) {
    state.setAttribute(key, values)
  }

  override suspend fun assistantCreateRun(runObject: RunObject, source: String) =
    assistantState.runSpan(runObject, source)

  override suspend fun assistantCreateRunStep(runObject: RunStepObject, source: String) =
    assistantState.runStepSpan(runObject, source)

  override suspend fun assistantCreatedMessage(messageObject: MessageObject, source: String) {
    assistantState.createdMessagesSpan(messageObject, source)
  }

  private fun getTracer(scopeName: String? = null): Tracer =
    openTelemetry.getTracer(scopeName ?: config.defaultScopeName)
}
