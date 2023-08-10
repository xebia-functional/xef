package com.xebia.functional.xef.prompt.expressions

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

class Expression(
  private val scope: CoreAIScope,
  private val model: ChatWithFunctions,
  val block: suspend Expression.() -> Unit
) {

  private val logger: KLogger = KotlinLogging.logger {}

  private val messages: MutableList<Message> = mutableListOf()

  private val generationKeys: MutableList<String> = mutableListOf()

  suspend fun system(message: suspend () -> String) {
    messages.add(Message.systemMessage(message))
  }

  suspend fun user(message: suspend () -> String) {
    messages.add(Message.userMessage(message))
  }

  suspend fun assistant(message: suspend () -> String) {
    messages.add(Message.assistantMessage(message))
  }

  fun prompt(key: String): String {
    generationKeys.add(key)
    return "{{$key}}"
  }

  suspend fun run(
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): ExpressionResult {
    block()
    val prelude =
      listOf(
        Message.systemMessage { "You are an expert in replacing variables in templates" },
      )
    val instructionMessages =
      listOf(
        Message.assistantMessage { "I will replace all placeholders in the message" },
      )
    val values: ReplacedValues =
      model.prompt(
        messages = prelude + messages + instructionMessages,
        context = scope.context,
        serializer = ReplacedValues.serializer(),
        conversationId = scope.conversationId,
        promptConfiguration = promptConfiguration
      )
    logger.info { "replaced: ${values.replacements.joinToString { it.key }}" }
    val replacedTemplate =
      messages.fold("") { acc, message ->
        val replacedMessage =
          generationKeys.fold(message.content) { acc, key ->
            acc.replace(
              "{{$key}}",
              values.replacements.firstOrNull { it.key == key }?.value ?: "{{$key}}"
            )
          }
        acc + replacedMessage + "\n"
      }
    return ExpressionResult(messages = messages, result = replacedTemplate, values = values)
  }

  companion object {
    suspend fun run(
      scope: CoreAIScope,
      model: ChatWithFunctions,
      block: suspend Expression.() -> Unit
    ): ExpressionResult = Expression(scope, model, block).run()
  }
}
