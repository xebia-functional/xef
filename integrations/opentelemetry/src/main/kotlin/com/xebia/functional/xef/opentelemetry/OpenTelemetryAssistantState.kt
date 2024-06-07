package com.xebia.functional.xef.opentelemetry

import com.xebia.functional.openai.generated.model.*
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context

class OpenTelemetryAssistantState(private val tracer: Tracer) {

  private val runStartedSource = "RunCreated"

  private val runFinishedSources =
    setOf("RunCompleted", "RunCancelled", "RunFailed", "RunIncomplete", "RunExpired")

  private val runStepStartedSource = "RunStepCreated"

  private val runStepFinishedSources =
    setOf("RunStepCompleted", "RunStepCancelled", "RunStepFailed", "RunStepExpired")

  private val messageStartedSource = "MessageCreated"

  private val messageFinishedSources = setOf("MessageCompleted", "MessageIncomplete")

  private val runIds: MutableMap<String, Context> = mutableMapOf()

  private val runSpans: MutableMap<String, Span> = mutableMapOf()

  private val runStepsSpans: MutableMap<String, Span> = mutableMapOf()

  private val messagesSpans: MutableMap<String, Span> = mutableMapOf()

  fun runSpan(runObject: RunObject, source: String) {

    val parentOrRoot: Context = runObject.id.getOrCreateContext()

    val currentSpan =
      tracer.spanBuilder(source).setParent(parentOrRoot).setSpanKind(SpanKind.CLIENT).startSpan()

    if (source == runStartedSource) {
      runSpans[runObject.id] = currentSpan
    }

    try {
      currentSpan.makeCurrent().use { runObject.setParameters(currentSpan) }
    } finally {
      if (runFinishedSources.contains(source) && runSpans[runObject.id] != null) {
        runSpans[runObject.id]!!.let {
          runObject.setParameters(it)
          it.updateName("RunCreated -> $source ${runObject.id}")
          it.end()
          runSpans.remove(runObject.id)
        }
      } else if (source != runStartedSource) {
        currentSpan.end()
      }
    }
  }

  fun runStepSpan(runStepObject: RunStepObject, source: String) {

    val parentOrRoot: Context = runStepObject.runId.getOrCreateContext()

    val currentSpan =
      tracer.spanBuilder(source).setParent(parentOrRoot).setSpanKind(SpanKind.CLIENT).startSpan()

    if (source == runStepStartedSource) {
      runStepsSpans[runStepObject.id] = currentSpan
    }

    try {
      currentSpan.makeCurrent().use { runStepObject.setParameters(currentSpan, source) }
    } finally {
      if (runStepFinishedSources.contains(source) && runStepsSpans[runStepObject.id] != null) {
        runStepsSpans[runStepObject.id]!!.let {
          runStepObject.setParameters(it, source)
          it.updateName("RunStepCreated -> $source ${runStepObject.id}")
          it.end()
          runStepsSpans.remove(runStepObject.id)
        }
      } else if (source != runStepStartedSource) {
        currentSpan.end()
      }
    }
  }

  fun createdMessagesSpan(messageObject: MessageObject, source: String) {
    val runId = messageObject.runId ?: return

    val parentOrRoot: Context = runId.getOrCreateContext()

    val currentSpan =
      tracer.spanBuilder(source).setParent(parentOrRoot).setSpanKind(SpanKind.CLIENT).startSpan()

    if (source == messageStartedSource) {
      messagesSpans[messageObject.id] = currentSpan
    }

    try {
      currentSpan.makeCurrent().use { messageObject.setParameters(currentSpan, source) }
    } finally {
      if (messageFinishedSources.contains(source) && messagesSpans[messageObject.id] != null) {
        messagesSpans[messageObject.id]!!.let {
          messageObject.setParameters(it, source)
          it.updateName("MessageCreated -> $source ${messageObject.id}")
          it.end()
          messagesSpans.remove(messageObject.id)
        }
      } else if (source != messageStartedSource) {
        currentSpan.end()
      }
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

  private fun RunStepObject.setParameters(span: Span, source: String) {
    span.setAttribute("openai.assistant.source", source)
    span.setAttribute("openai.assistant.type", type.name)
    span.setAttribute("openai.assistant.thread.id", threadId)
    span.setAttribute("openai.assistant.assistant.id", assistantId)
    span.setAttribute("openai.assistant.run.id", runId)
    span.setAttribute("openai.assistant.runStep.id", id)
    span.setAttribute("openai.assistant.status", status.name)
    when (val detail = stepDetails) {
      is RunStepObjectStepDetails.CaseRunStepDetailsMessageCreationObject -> {
        span.setAttribute(
          "openai.assistant.messageCreation.id",
          detail.value.messageCreation.messageId
        )
      }
      is RunStepObjectStepDetails.CaseRunStepDetailsToolCallsObject -> {
        detail.value.toolCalls.forEachIndexed { index, toolCall ->
          when (toolCall) {
            is RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsCodeObject -> {
              span.setAttribute("openai.assistant.toolCalls.$index.type", toolCall.value.type.name)
              span.setAttribute(
                "openai.assistant.toolCalls.$index.function.name",
                "code_interpreter"
              )
            }
            is RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFunctionObject -> {
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
            is RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFileSearchObject -> {
              span.setAttribute("openai.assistant.toolCalls.$index.type", toolCall.value.type.name)
              span.setAttribute("openai.assistant.toolCalls.$index.function.name", "retrieval")
            }
          }
        }
      }
    }
  }

  private fun MessageObject.setParameters(span: Span, source: String) {
    span.setAttribute("openai.assistant.message.source", source)
    span.setAttribute("openai.assistant.message.role", role.name)
    span.setAttribute("openai.assistant.message.thread.id", threadId)
    assistantId?.let { span.setAttribute("openai.assistant.message.assistant.id", it) }
    runId?.let { span.setAttribute("openai.assistant.message.run.id", it) }
    span.setAttribute("openai.assistant.message.id", id)
    status?.let { span.setAttribute("openai.assistant.message.status", it.name) }
    when (val inner = content.firstOrNull()) {
      is MessageObjectContentInner.CaseMessageContentImageFileObject -> {
        span.setAttribute("openai.assistant.message.content", inner.value.imageFile.fileId)
      }
      is MessageObjectContentInner.CaseMessageContentTextObject -> {
        span.setAttribute("openai.assistant.message.content", inner.value.text.value)
      }
      is MessageObjectContentInner.CaseMessageContentImageUrlObject ->
        span.setAttribute("openai.assistant.message.content", inner.value.imageUrl.url)
      null -> {}
    }
  }
}
