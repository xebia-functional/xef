package com.xebia.functional.xef.auto.llm.openai

import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.serialization.serializer

@AiDsl
suspend fun Conversation.promptMessage(
  prompt: Prompt,
  model: Chat = OpenAI().DEFAULT_CHAT
): String = model.promptMessage(prompt, this)

@AiDsl
suspend fun Conversation.promptMessage(
  prompt: Prompt,
  model: Chat = OpenAI().DEFAULT_CHAT,
  functions: List<CFunction> = emptyList()
): List<String> = model.promptMessages(prompt, this, functions)

@AiDsl
suspend inline fun <reified A, reified B> Conversation.prompt(
  input: A,
  model: ChatWithFunctions = OpenAI().DEFAULT_SERIALIZATION
): B {
  return model.prompt(
    input = input,
    scope = conversation,
    inputSerializer = serializer<A>(),
    outputSerializer = serializer<B>()
  )
}

@AiDsl
suspend inline fun <reified A> Conversation.prompt(
  prompt: Prompt,
  model: ChatWithFunctions = OpenAI().DEFAULT_SERIALIZATION
): A = prompt(model = model, prompt = prompt, serializer = serializer<A>())
