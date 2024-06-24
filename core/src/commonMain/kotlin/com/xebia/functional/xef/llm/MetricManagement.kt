package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.assistants.RunDelta
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.completionRole
import com.xebia.functional.xef.prompt.contentAsString
import io.github.nomisrev.openapi.CreateChatCompletionResponse
import io.github.nomisrev.openapi.RunObject
import io.github.nomisrev.openapi.RunStepObject

suspend fun CreateChatCompletionResponse.addMetrics(
  conversation: Conversation
): CreateChatCompletionResponse {
  conversation.metric.parameter("openai.chat_completion.model", model)
  usage?.let {
    conversation.metric.parameter("openai.chat_completion.prompt.token.count", "${it.promptTokens}")
    conversation.metric.parameter(
      "openai.chat_completion.completion.token.count",
      "${it.completionTokens}"
    )
    conversation.metric.parameter("openai.chat_completion.token.count", "${it.totalTokens}")
  }
  choices.forEach { choice ->
    choice.message.content?.let {
      conversation.metric.parameter("openai.chat_completion.choice.${choice.index}.content", it)
    }
    choice.message.toolCalls?.zip(choice.message.toolCalls!!.indices)?.forEach {
      (toolCall, toolCallIndex) ->
      conversation.metric.parameter(
        "openai.chat_completion.choice.${choice.index}.tool_call.$toolCallIndex",
        toolCall.function.arguments
      )
    }
  }
  return this
}

suspend fun Prompt.addMetrics(conversation: Conversation) {
  conversation.metric.parameter("openai.chat_completion.prompt.message.count", "${messages.size}")

  conversation.metric.parameter(
    "openai.chat_completion.prompt.messages_roles",
    messages.map { it.completionRole().value }
  )
  conversation.metric.parameter(
    "openai.chat_completion.prompt.last-message",
    messages.lastOrNull()?.contentAsString() ?: "empty"
  )
  conversation.metric.parameter(
    "openai.chat_completion.conversation_id",
    conversation.conversationId?.value ?: "none"
  )
  conversation.metric.parameter(
    "openai.chat_completion.prompt.temperature",
    "${configuration.temperature}"
  )
  if (functions.isNotEmpty())
    conversation.metric.parameter("openai.chat_completion.functions", functions.map { it.name })
}

suspend fun RunObject.addMetrics(metric: Metric): RunObject {
  metric.assistantCreateRun(this)
  return this
}

suspend fun RunStepObject.addMetrics(metric: Metric): RunStepObject {
  metric.assistantCreateRunStep(this)
  return this
}

suspend fun RunDelta.addMetrics(metric: Metric): RunDelta {
  when (this) {
    is RunDelta.RunCancelled -> run.addMetrics(metric)
    is RunDelta.RunCancelling -> run.addMetrics(metric)
    is RunDelta.RunCompleted -> run.addMetrics(metric)
    is RunDelta.RunCreated -> run.addMetrics(metric)
    is RunDelta.RunExpired -> run.addMetrics(metric)
    is RunDelta.RunFailed -> run.addMetrics(metric)
    is RunDelta.RunInProgress -> run.addMetrics(metric)
    is RunDelta.RunQueued -> run.addMetrics(metric)
    is RunDelta.RunRequiresAction -> run.addMetrics(metric)
    is RunDelta.RunStepCancelled -> runStep.addMetrics(metric)
    is RunDelta.RunStepCompleted -> runStep.addMetrics(metric)
    is RunDelta.RunStepCreated -> runStep.addMetrics(metric)
    is RunDelta.RunStepExpired -> runStep.addMetrics(metric)
    is RunDelta.RunStepFailed -> runStep.addMetrics(metric)
    is RunDelta.RunStepInProgress -> runStep.addMetrics(metric)
    else -> {} // ignore other cases
  }
  return this
}
