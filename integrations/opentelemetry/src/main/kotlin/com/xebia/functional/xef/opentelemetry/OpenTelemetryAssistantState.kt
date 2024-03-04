package com.xebia.functional.xef.opentelemetry

import com.xebia.functional.openai.models.RunObject
import com.xebia.functional.openai.models.RunStepDetailsToolCallsObjectToolCallsInner
import com.xebia.functional.openai.models.RunStepObject
import com.xebia.functional.openai.models.ext.assistant.RunStepDetailsMessageCreationObject
import com.xebia.functional.openai.models.ext.assistant.RunStepDetailsToolCallsObject
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context

class OpenTelemetryAssistantState(private val tracer: Tracer) {

  private val runIds: MutableMap<String, Context> = mutableMapOf()

  fun runSpan(runObject: RunObject) {

    val parentOrRoot: Context = runObject.id.getOrCreateContext()

    val currentSpan =
      tracer
        .spanBuilder(runObject.status.value)
        .setParent(parentOrRoot)
        .setSpanKind(SpanKind.CLIENT)
        .startSpan()

    try {
      currentSpan.makeCurrent().use { runObject.setParameters(currentSpan) }
    } finally {
      currentSpan.end()
    }
  }

  fun runSpan(runId: String, block: () -> RunObject): RunObject {

    val parentOrRoot: Context = runId.getOrCreateContext()

    val currentSpan =
      tracer
        .spanBuilder("New Run: $runId")
        .setParent(parentOrRoot)
        .setSpanKind(SpanKind.CLIENT)
        .startSpan()

    return try {
      val output = block()
      currentSpan.makeCurrent().use {
        currentSpan.updateName(output.status.value)
        output.setParameters(currentSpan)
      }
      output
    } finally {
      currentSpan.end()
    }
  }

  suspend fun toolOutputRunSpan(runId: String, block: suspend () -> RunObject): RunObject {

    val parentOrRoot: Context = runId.getOrCreateContext()

    val currentSpan =
      tracer
        .spanBuilder("New ToolOutput: $runId")
        .setParent(parentOrRoot)
        .setSpanKind(SpanKind.CLIENT)
        .startSpan()

    return try {
      val output = block()
      currentSpan.makeCurrent().use {
        currentSpan.updateName("ToolOutput: ${output.status.value}")
        output.setParameters(currentSpan)
      }
      output
    } finally {
      currentSpan.end()
    }
  }

  fun runStepSpan(runId: String, block: () -> RunStepObject): RunStepObject {

    val parentOrRoot: Context = runId.getOrCreateContext()

    val currentSpan =
      tracer
        .spanBuilder("New RunStep: $runId")
        .setParent(parentOrRoot)
        .setSpanKind(SpanKind.CLIENT)
        .startSpan()

    return try {
      val output = block()
      currentSpan.makeCurrent().use {
        when (val detail = output.stepDetails) {
          is RunStepDetailsMessageCreationObject -> {
            currentSpan.updateName("Creating message: ${output.status.value}")
          }
          is RunStepDetailsToolCallsObject -> {
            currentSpan.updateName("Tools: ${detail.toolCalls.joinToString { 
              when (it.type) {
                RunStepDetailsToolCallsObjectToolCallsInner.Type.code_interpreter -> it.type.value
                RunStepDetailsToolCallsObjectToolCallsInner.Type.retrieval -> it.type.value
                RunStepDetailsToolCallsObjectToolCallsInner.Type.function -> it.function?.name ?: it.type.value
              }
            }}: ${output.status.value}")
          }
        }
        output.setParameters(currentSpan)
      }
      output
    } finally {
      currentSpan.end()
    }
  }

  private fun String.getOrCreateContext(): Context {
    val parent = runIds.get(this)
    return if (parent == null) {
      val newParent = tracer.spanBuilder("Run: $this").startSpan()
      newParent.end()
      val newContext = Context.current().with(newParent)
      runIds[this] = newContext
      newContext
    } else parent
  }

  private fun RunObject.setParameters(span: Span) {
    span.setAttribute("openai.assistant.model", model)
    span.setAttribute("openai.assistant.fileIds", fileIds.joinToString())
    span.setAttribute("openai.assistant.tools.count", tools.count().toString())
    span.setAttribute("openai.assistant.thread.id", threadId)
    span.setAttribute("openai.assistant.assistant.id", assistantId)
    span.setAttribute("openai.assistant.run.id", id)
    span.setAttribute("openai.assistant.status", status.value)
  }

  private fun RunStepObject.setParameters(span: Span) {
    span.setAttribute("openai.assistant.type", type.value)
    span.setAttribute("openai.assistant.thread.id", threadId)
    span.setAttribute("openai.assistant.assistant.id", assistantId)
    span.setAttribute("openai.assistant.run.id", runId)
    span.setAttribute("openai.assistant.runStep.id", id)
    span.setAttribute("openai.assistant.status", status.value)
    when (val detail = stepDetails) {
      is RunStepDetailsMessageCreationObject -> {
        span.setAttribute("openai.assistant.messageCreation.id", detail.messageCreation.messageId)
      }
      is RunStepDetailsToolCallsObject -> {
        detail.toolCalls.forEachIndexed { index, toolCall ->
          span.setAttribute("openai.assistant.toolCalls.$index.type", toolCall.type.value)
          span.setAttribute("openai.assistant.toolCalls.$index.function.name", toolCall.function?.name ?: "")
          span.setAttribute("openai.assistant.toolCalls.$index.function.arguments", toolCall.function?.arguments ?: "")
        }
      }
    }
  }
}
