package com.xebia.functional.xef.opentelemetry

import com.xebia.functional.xef.openapi.*
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
        .spanBuilder(runObject.status.name)
        .setParent(parentOrRoot)
        .setSpanKind(SpanKind.CLIENT)
        .startSpan()

    try {
      currentSpan.makeCurrent().use { runObject.setParameters(currentSpan) }
    } finally {
      currentSpan.end()
    }
  }

  suspend fun runSpan(runId: String, block: suspend () -> RunObject): RunObject {

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
        currentSpan.updateName(output.status.name)
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
        currentSpan.updateName("ToolOutput: ${output.status.name}")
        output.setParameters(currentSpan)
      }
      output
    } finally {
      currentSpan.end()
    }
  }

  fun runStepSpan(runObject: RunStepObject) {

    val parentOrRoot: Context = runObject.runId.getOrCreateContext()

    val currentSpan =
      tracer
        .spanBuilder("step ${runObject.status.name} ${runObject.id}")
        .setParent(parentOrRoot)
        .setSpanKind(SpanKind.CLIENT)
        .startSpan()

    try {
      currentSpan.makeCurrent().use { runObject.setParameters(currentSpan) }
    } finally {
      currentSpan.end()
    }
  }

  suspend fun runStepSpan(runId: String, block: suspend () -> RunStepObject): RunStepObject {

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
          is RunStepObject.StepDetails.CaseRunStepDetailsMessageCreationObject ->
            currentSpan.updateName("Creating message: ${output.status.name}")
          is RunStepObject.StepDetails.CaseRunStepDetailsToolCallsObject ->
            currentSpan.updateName(
              "Tools: ${detail.value.toolCalls.joinToString {
                when (it) {
                  is RunStepDetailsToolCallsObject.ToolCalls.CaseRunStepDetailsToolCallsCodeObject -> it.value.type.name
                  is RunStepDetailsToolCallsObject.ToolCalls.CaseRunStepDetailsToolCallsFunctionObject -> it.value.function.name ?: ""
                  is RunStepDetailsToolCallsObject.ToolCalls.CaseRunStepDetailsToolCallsRetrievalObject -> it.value.type.name
                }
              }}: ${output.status.name}"
            )
        }
        output.setParameters(currentSpan)
      }
      output
    } finally {
      currentSpan.end()
    }
  }

  suspend fun createdMessagesSpan(
    runId: String,
    block: suspend () -> List<MessageObject>
  ): List<MessageObject> {

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
        currentSpan.updateName("Messages: ${output.size}")
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
    if (fileIds.isNotEmpty()) span.setAttribute("openai.assistant.fileIds", fileIds.joinToString())
    span.setAttribute("openai.assistant.tools.count", tools.count().toString())
    span.setAttribute("openai.assistant.thread.id", threadId)
    span.setAttribute("openai.assistant.assistant.id", assistantId)
    span.setAttribute("openai.assistant.run.id", id)
    span.setAttribute("openai.assistant.status", status.name)
    usage?.let {
      span.setAttribute("openai.assistant.usage.totalTokens", it.totalTokens.toString())
      span.setAttribute("openai.assistant.usage.completionTokens", it.completionTokens.toString())
      span.setAttribute("openai.assistant.usage.promptTokens", it.promptTokens.toString())
    }
  }

  private fun RunStepObject.setParameters(span: Span) {
    span.setAttribute("openai.assistant.type", type.name)
    span.setAttribute("openai.assistant.thread.id", threadId)
    span.setAttribute("openai.assistant.assistant.id", assistantId)
    span.setAttribute("openai.assistant.run.id", runId)
    span.setAttribute("openai.assistant.runStep.id", id)
    span.setAttribute("openai.assistant.status", status.name)
    when (val detail = stepDetails) {
      is RunStepObject.StepDetails.CaseRunStepDetailsMessageCreationObject -> {
        span.setAttribute(
          "openai.assistant.messageCreation.id",
          detail.value.messageCreation.messageId
        )
      }
      is RunStepObject.StepDetails.CaseRunStepDetailsToolCallsObject -> {
        detail.value.toolCalls.forEachIndexed { index, toolCall ->
          when (toolCall) {
            is RunStepDetailsToolCallsObject.ToolCalls.CaseRunStepDetailsToolCallsCodeObject -> {
              span.setAttribute("openai.assistant.toolCalls.$index.type", toolCall.value.type.name)
              span.setAttribute(
                "openai.assistant.toolCalls.$index.function.name",
                "code_interpreter"
              )
            }
            is RunStepDetailsToolCallsObject.ToolCalls.CaseRunStepDetailsToolCallsFunctionObject -> {
              span.setAttribute("openai.assistant.toolCalls.$index.type", toolCall.value.type.name)
              span.setAttribute(
                "openai.assistant.toolCalls.$index.function.name",
                toolCall.value.function.name ?: ""
              )
              span.setAttribute(
                "openai.assistant.toolCalls.$index.function.arguments",
                toolCall.value.function.arguments ?: ""
              )
            }
            is RunStepDetailsToolCallsObject.ToolCalls.CaseRunStepDetailsToolCallsRetrievalObject -> {
              span.setAttribute("openai.assistant.toolCalls.$index.type", toolCall.value.type.name)
              span.setAttribute("openai.assistant.toolCalls.$index.function.name", "retrieval")
            }
          }
        }
      }
    }
  }

  private fun List<MessageObject>.setParameters(span: Span) {
    span.setAttribute("openai.assistant.messages.count", size.toString())
    forEach {
      span.setAttribute("openai.assistant.messages.${indexOf(it)}.role", it.role.name)
      when (val inner = it.content.firstOrNull()) {
        is MessageObject.Content.CaseMessageContentImageFileObject -> {
          span.setAttribute(
            "openai.assistant.messages.${indexOf(it)}.content",
            inner.value.imageFile.fileId
          )
        }
        is MessageObject.Content.CaseMessageContentTextObject -> {
          span.setAttribute(
            "openai.assistant.messages.${indexOf(it)}.content",
            inner.value.text.value
          )
        }
        null -> {}
      }
    }
  }
}
