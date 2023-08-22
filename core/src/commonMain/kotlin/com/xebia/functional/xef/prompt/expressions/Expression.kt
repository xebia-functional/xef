package com.xebia.functional.xef.prompt.expressions

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

class Expression(
  private val scope: Conversation,
  private val model: ChatWithFunctions,
  val block: suspend Expression.() -> Unit
) {

  private val logger: KLogger = KotlinLogging.logger {}

  private val messages: MutableList<Message> = mutableListOf()

  private val generationKeys: MutableList<String> = mutableListOf()

  fun addMessages(newMessages: List<Message>) {
    messages.addAll(newMessages)
  }

  fun prompt(key: String): String {
    generationKeys.add(key)
    return "{{$key}}"
  }

  suspend fun run(): ExpressionResult {
    block()
    val prelude = Prompt { +system("You are an expert in replacing variables in templates") }

    val instructionMessages = Prompt {
      +assistant("I will replace all placeholders in the message")
    }

    val values: ReplacedValues =
      model.prompt(
        prompt = Prompt(prelude.messages + messages + instructionMessages.messages),
        scope = scope,
        serializer = ReplacedValues.serializer(),
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
      scope: Conversation,
      model: ChatWithFunctions,
      block: suspend Expression.() -> Unit
    ): ExpressionResult = Expression(scope, model, block).run()
  }
}
