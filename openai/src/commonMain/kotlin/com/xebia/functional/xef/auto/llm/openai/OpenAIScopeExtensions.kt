package com.xebia.functional.xef.auto.llm.openai

import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.serialization.serializer

@AiDsl
suspend fun Conversation.promptMessage(
  prompt: String,
  model: Chat = OpenAI().DEFAULT_CHAT,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): String = model.promptMessage(prompt, this, promptConfiguration)

@AiDsl
suspend fun Conversation.promptMessage(
  prompt: String,
  model: Chat = OpenAI().DEFAULT_CHAT,
  functions: List<CFunction> = emptyList(),
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): List<String> = model.promptMessages(prompt, this, functions, promptConfiguration)

@AiDsl
suspend fun Conversation.promptMessage(
  prompt: Prompt,
  model: Chat = OpenAI().DEFAULT_CHAT,
  functions: List<CFunction> = emptyList(),
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): List<String> = model.promptMessages(prompt, this, functions, promptConfiguration)

@AiDsl
suspend inline fun <reified A> Conversation.prompt(
  model: ChatWithFunctions = OpenAI().DEFAULT_SERIALIZATION,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A =
  prompt(
    model = model,
    prompt = Prompt(""),
    serializer = serializer<A>(),
    promptConfiguration = promptConfiguration
  )

@AiDsl
suspend inline fun <reified A> Conversation.prompt(
  prompt: String,
  model: ChatWithFunctions = OpenAI().DEFAULT_SERIALIZATION,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A =
  prompt(
    model = model,
    prompt = Prompt(prompt),
    serializer = serializer<A>(),
    promptConfiguration = promptConfiguration
  )

@AiDsl
suspend inline fun <reified A> Conversation.prompt(
  prompt: Prompt,
  model: ChatWithFunctions = OpenAI().DEFAULT_SERIALIZATION,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A =
  prompt(
    model = model,
    prompt = prompt,
    serializer = serializer<A>(),
    promptConfiguration = promptConfiguration
  )

@AiDsl
suspend inline fun <reified A> Conversation.prompt(
  messages: List<Message>,
  model: ChatWithFunctions = OpenAI().DEFAULT_SERIALIZATION,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A =
  prompt(
    model = model,
    messages = messages,
    serializer = serializer<A>(),
    promptConfiguration = promptConfiguration
  )

@AiDsl
suspend inline fun <reified A> Conversation.image(
  prompt: String,
  model: ChatWithFunctions = OpenAI().DEFAULT_SERIALIZATION,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A =
  prompt(
    model = model,
    prompt = Prompt(prompt),
    serializer = serializer<A>(),
    promptConfiguration = promptConfiguration
  )
