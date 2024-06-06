package com.xebia.functional.xef.llm

import com.xebia.functional.openai.generated.model.CreateChatCompletionResponse
import com.xebia.functional.openai.generated.model.MessageObject
import com.xebia.functional.openai.generated.model.RunObject
import com.xebia.functional.openai.generated.model.RunStepObject
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.assistants.RunDelta
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.completionRole
import com.xebia.functional.xef.prompt.contentAsString

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

suspend fun RunObject.addMetrics(metric: Metric, source: String): RunObject {
  metric.assistantCreateRun(this, source)
  return this
}

suspend fun RunStepObject.addMetrics(metric: Metric, source: String): RunStepObject {
  metric.assistantCreateRunStep(this, source)
  return this
}

suspend fun MessageObject.addMetrics(metric: Metric, source: String): MessageObject {
  metric.assistantCreatedMessage(this, source)
  return this
}

suspend fun RunDelta.addMetrics(metric: Metric): RunDelta {
  when (this) {
    is RunDelta.RunCancelled -> run.addMetrics(metric, "RunCancelled")
    is RunDelta.RunCancelling -> run.addMetrics(metric, "RunCancelling")
    is RunDelta.RunCompleted -> run.addMetrics(metric, "RunCompleted")
    is RunDelta.RunCreated -> run.addMetrics(metric, "RunCreated")
    is RunDelta.RunExpired -> run.addMetrics(metric, "RunExpired")
    is RunDelta.RunFailed -> run.addMetrics(metric, "RunFailed")
    is RunDelta.RunInProgress -> run.addMetrics(metric, "RunInProgress")
    is RunDelta.RunQueued -> run.addMetrics(metric, "RunQueued")
    is RunDelta.RunRequiresAction -> run.addMetrics(metric, "RunRequiresAction")
    is RunDelta.RunStepCancelled -> runStep.addMetrics(metric, "RunStepCancelled")
    is RunDelta.RunStepCompleted -> runStep.addMetrics(metric, "RunStepCompleted")
    is RunDelta.RunStepCreated -> runStep.addMetrics(metric, "RunStepCreated")
    is RunDelta.RunStepExpired -> runStep.addMetrics(metric, "RunStepExpired")
    is RunDelta.RunStepFailed -> runStep.addMetrics(metric, "RunStepFailed")
    is RunDelta.RunStepInProgress -> runStep.addMetrics(metric, "RunStepInProgress")
    is RunDelta.MessageCreated -> message.addMetrics(metric, "MessageCreated")
    is RunDelta.MessageIncomplete -> message.addMetrics(metric, "MessageIncomplete")
    is RunDelta.MessageCompleted -> message.addMetrics(metric, "MessageCompleted")
    is RunDelta.MessageInProgress -> message.addMetrics(metric, "MessageInProgress")
    else -> {} // ignore other cases
  }
  return this
}
