package com.xebia.functional.xef.conversation.llm.openai

import com.aallam.openai.api.chat.ChatChunk as OAIChatChunk
import com.aallam.openai.api.chat.ChatCompletionChunk as OAIChatCompletionChunk
import com.aallam.openai.api.chat.ChatDelta as OAIChatDelta
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.core.Usage as OAIUsage
import com.xebia.functional.xef.llm.models.chat.ChatChunk
import com.xebia.functional.xef.llm.models.chat.ChatCompletionChunk
import com.xebia.functional.xef.llm.models.chat.ChatDelta
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.llm.models.functions.FunctionCall
import com.xebia.functional.xef.llm.models.usage.Usage

internal fun ChatRole.toInternal() =
  when (this) {
    ChatRole.User -> Role.USER
    ChatRole.Assistant -> Role.ASSISTANT
    ChatRole.System -> Role.SYSTEM
    ChatRole.Function -> Role.SYSTEM
    else -> Role.ASSISTANT
  }

internal fun Role.toOpenAI() =
  when (this) {
    Role.USER -> ChatRole.User
    Role.ASSISTANT -> ChatRole.Assistant
    Role.SYSTEM -> ChatRole.System
  }

internal fun OAIUsage?.toInternal() =
  Usage(
    promptTokens = this?.promptTokens,
    completionTokens = this?.completionTokens,
    totalTokens = this?.totalTokens,
  )

internal fun OAIChatChunk.toInternal() =
  ChatChunk(
    index = index,
    delta = delta.toInternal(),
    finishReason = finishReason?.value,
  )

internal fun OAIChatCompletionChunk.toInternal() =
  ChatCompletionChunk(
    id = id,
    created = created,
    model = model.id,
    choices = choices.map(com.aallam.openai.api.chat.ChatChunk::toInternal),
    usage = usage.toInternal()
  )

internal fun OAIChatDelta.toInternal() =
  ChatDelta(
    role = role?.toInternal(),
    content = content,
    functionCall =
      functionCall?.let {
        if (it.nameOrNull == null || it.argumentsOrNull == null) null
        else FunctionCall(it.name, it.arguments)
      }
  )
