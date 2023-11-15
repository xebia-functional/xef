package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatRole
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role

@OptIn(BetaOpenAI::class)
fun ChatCompletionRequest.toCore(): com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest =
  com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest(
    messages = messages.map { Message(it.role.toCore(), it.content ?: "", it.name ?: "") },
    temperature = temperature ?: 0.0,
    topP = topP ?: 1.0,
    n = n ?: 1,
    stream = false,
    stop = stop,
    maxTokens = maxTokens,
    presencePenalty = presencePenalty ?: 0.0,
    frequencyPenalty = frequencyPenalty ?: 0.0,
    logitBias = logitBias ?: emptyMap(),
    user = user,
    streamToStandardOut = false
  )

@OptIn(BetaOpenAI::class)
fun ChatRole.toCore(): Role =
  when (this) {
    ChatRole.System -> Role.SYSTEM
    ChatRole.User -> Role.USER
    ChatRole.Assistant -> Role.ASSISTANT
    else -> Role.ASSISTANT
  }
