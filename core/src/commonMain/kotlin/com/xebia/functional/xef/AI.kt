package com.xebia.functional.xef

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.llm.models.modelType
import com.xebia.functional.xef.llm.prompt
import com.xebia.functional.xef.llm.promptStreaming
import com.xebia.functional.xef.openapi.CreateChatCompletionRequest
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class AI<out A>(private val config: AIConfig, val serializer: Tool<A>) {

  private fun runStreamingWithStringSerializer(prompt: Prompt): Flow<String> =
    config.api.promptStreaming(prompt, config.conversation, config.tools)

  @PublishedApi
  internal suspend operator fun invoke(prompt: Prompt): A =
    when (val serializer = serializer) {
      is Tool.Callable -> config.api.prompt(prompt, config.conversation, serializer, config.tools)
      is Tool.Contextual -> config.api.prompt(prompt, config.conversation, serializer, config.tools)
      is Tool.Enumeration<A> -> runWithEnumSingleTokenSerializer(serializer, prompt)
      is Tool.FlowOfStreamedFunctions<*> -> {
        config.api.promptStreaming(prompt, config.conversation, serializer, config.tools) as A
      }
      is Tool.FlowOfStrings -> runStreamingWithStringSerializer(prompt) as A
      is Tool.Primitive -> config.api.prompt(prompt, config.conversation, serializer, config.tools)
      is Tool.Sealed -> config.api.prompt(prompt, config.conversation, serializer, config.tools)
      is Tool.FlowOfAIEventsSealed ->
        channelFlow {
          send(AIEvent.Start)
          config.api.prompt(
            prompt = prompt,
            scope = config.conversation,
            serializer = serializer.sealedSerializer,
            tools = config.tools,
            collector = this
          )
        }
          as A
      is Tool.FlowOfAIEvents ->
        channelFlow {
          send(AIEvent.Start)
          config.api.prompt(
            prompt = prompt,
            scope = config.conversation,
            serializer = serializer.serializer,
            tools = config.tools,
            collector = this
          )
        }
          as A
    }

  private suspend fun runWithEnumSingleTokenSerializer(
    serializer: Tool.Enumeration<A>,
    prompt: Prompt
  ): A {
    val encoding = prompt.model.modelType(forFunctions = false).encoding
    val cases = serializer.cases
    val logitBias =
      JsonObject(
        cases
          .flatMap {
            val result = encoding.encode(it.function.name)
            if (result.size > 1) {
              error("Cannot encode enum case $it into one token")
            }
            result
          }
          .associate { "$it" to JsonPrimitive(100) }
      )
    val result =
      config.api.completions.createChatCompletion(
        CreateChatCompletionRequest(
          messages = prompt.messages,
          model = prompt.model,
          logitBias = logitBias,
          maxTokens = 1,
          temperature = 0.0
        )
      )
    val choice = result.choices[0].message.content
    val enumSerializer = serializer.enumSerializer
    return if (choice != null) {
      enumSerializer(choice)
    } else {
      error("Cannot decode enum case from $choice")
    }
  }

  companion object {
    @AiDsl
    suspend inline fun <reified E> classify(
      input: String,
      output: String,
      context: String,
      config: AIConfig = AIConfig(),
    ): E where E : Enum<E>, E : PromptClassifier {
      val value = enumValues<E>().firstOrNull() ?: error("No values to classify")
      return AI<E>(
        prompt = value.template(input, output, context),
        config = config,
      )
    }

    @AiDsl
    suspend inline fun <reified E> multipleClassify(
      input: String,
      config: AIConfig = AIConfig(),
    ): List<E> where E : Enum<E>, E : PromptMultipleClassifier {
      val values = enumValues<E>()
      val value = values.firstOrNull() ?: error("No values to classify")
      val selected: SelectedItems =
        AI(
          prompt = value.template(input),
          serializer = Tool.fromKotlin<SelectedItems>(),
          config = config
        )
      return selected.selectedItems.mapNotNull { values.elementAtOrNull(it) }
    }
  }
}

@AiDsl
suspend inline fun <reified A> AI(
  prompt: String,
  serializer: Tool<A> = Tool.fromKotlin<A>(),
  config: AIConfig = AIConfig()
): A = AI(Prompt(config.model, prompt), serializer, config)

@AiDsl
suspend inline fun <reified A> AI(
  prompt: Prompt,
  serializer: Tool<A> = Tool.fromKotlin<A>(),
  config: AIConfig = AIConfig(),
): A = AI(config, serializer).invoke(prompt)
