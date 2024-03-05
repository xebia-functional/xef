package com.xebia.functional.xef.opentelemetry

import com.xebia.functional.openai.models.MessageObject
import com.xebia.functional.openai.models.RunObject
import com.xebia.functional.openai.models.RunStepObject
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.prompt.Prompt
import io.opentelemetry.api.trace.*

class OpenTelemetryMetric(
  private val config: OpenTelemetryConfig = OpenTelemetryConfig.create("xef", "io.xef")
) : Metric {

  private val openTelemetry = config.newInstance()

  private val state = OpenTelemetryState(getTracer())

  private val assistantState = OpenTelemetryAssistantState(getTracer())

  override suspend fun <A> customSpan(name: String, block: suspend Metric.() -> A): A =
    state.span(name) { block() }

  override suspend fun <A, T> promptSpan(prompt: Prompt<T>, block: suspend Metric.() -> A): A =
    state.span("Prompt: ${prompt.messages.lastOrNull()?.contentAsString() ?: "empty"}") { block() }

  override suspend fun event(message: String) {
    state.event(message)
  }

  override suspend fun parameter(key: String, value: String) {
    state.setAttribute(key, value)
  }

  override suspend fun parameter(key: String, values: List<String>) {
    state.setAttribute(key, values)
  }

  override suspend fun assistantCreateRun(runObject: RunObject) = assistantState.runSpan(runObject)

  override suspend fun assistantCreateRun(
    runId: String,
    block: suspend Metric.() -> RunObject
  ): RunObject = assistantState.runSpan(runId) { block() }

  override suspend fun assistantCreatedMessage(
    runId: String,
    block: suspend Metric.() -> List<MessageObject>
  ): List<MessageObject> = assistantState.createdMessagesSpan(runId) { block() }

  override suspend fun assistantCreateRunStep(
    runId: String,
    block: suspend Metric.() -> RunStepObject
  ): RunStepObject = assistantState.runStepSpan(runId) { block() }

  override suspend fun assistantToolOutputsRun(
    runId: String,
    block: suspend Metric.() -> RunObject
  ): RunObject = assistantState.toolOutputRunSpan(runId) { block() }

  private fun getTracer(scopeName: String? = null): Tracer =
    openTelemetry.getTracer(scopeName ?: config.defaultScopeName)
}
