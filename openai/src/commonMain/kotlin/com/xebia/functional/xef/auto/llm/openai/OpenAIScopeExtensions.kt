package com.xebia.functional.xef.auto.llm.openai

import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.serialization.serializer

@AiDsl
suspend fun CoreAIScope.promptMessage(
  prompt: String,
  model: Chat = OpenAI.DEFAULT_CHAT,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): String = model.promptMessage(prompt, context, promptConfiguration)

@AiDsl
suspend fun CoreAIScope.promptMessage(
  prompt: String,
  model: Chat = OpenAI.DEFAULT_CHAT,
  functions: List<CFunction> = emptyList(),
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): List<String> = model.promptMessage(prompt, context, functions, promptConfiguration)

@AiDsl
suspend fun CoreAIScope.promptMessage(
  prompt: Prompt,
  model: Chat = OpenAI.DEFAULT_CHAT,
  functions: List<CFunction> = emptyList(),
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): List<String> = model.promptMessage(prompt, context, functions, promptConfiguration)

@AiDsl
suspend inline fun <reified A> CoreAIScope.prompt(
  model: ChatWithFunctions = OpenAI.DEFAULT_SERIALIZATION,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A =
  prompt(
    model = model,
    prompt = Prompt(""),
    serializer = serializer<A>(),
    promptConfiguration = promptConfiguration
  )

@AiDsl
suspend inline fun <reified A> CoreAIScope.prompt(
  prompt: String,
  model: ChatWithFunctions = OpenAI.DEFAULT_SERIALIZATION,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A =
  prompt(
    model = model,
    prompt = Prompt(prompt),
    serializer = serializer<A>(),
    promptConfiguration = promptConfiguration
  )

@AiDsl
suspend inline fun <reified A> CoreAIScope.prompt(
  prompt: Prompt,
  model: ChatWithFunctions = OpenAI.DEFAULT_SERIALIZATION,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A =
  prompt(
    model = model,
    prompt = prompt,
    serializer = serializer<A>(),
    promptConfiguration = promptConfiguration
  )

@AiDsl
suspend inline fun <reified A> CoreAIScope.image(
  prompt: String,
  model: ChatWithFunctions = OpenAI.DEFAULT_SERIALIZATION,
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A =
  prompt(
    model = model,
    prompt = Prompt(prompt),
    serializer = serializer<A>(),
    promptConfiguration = promptConfiguration
  )
