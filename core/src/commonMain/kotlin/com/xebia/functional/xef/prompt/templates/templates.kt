package com.xebia.functional.xef.prompt.templates

import com.xebia.functional.openai.models.ChatCompletionRole
import com.xebia.functional.openai.models.ChatCompletionRole.*
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import com.xebia.functional.xef.prompt.message

fun system(context: String): ChatCompletionRequestMessage = context.message(system)

fun assistant(context: String): ChatCompletionRequestMessage = context.message(assistant)

fun user(context: String): ChatCompletionRequestMessage = context.message(user)

inline fun <reified A> system(data: A): ChatCompletionRequestMessage = data.message(system)

inline fun <reified A> assistant(data: A): ChatCompletionRequestMessage = data.message(assistant)

inline fun <reified A> user(data: A): ChatCompletionRequestMessage = data.message(user)

fun steps(role: ChatCompletionRole, content: () -> List<String>): ChatCompletionRequestMessage =
  content().mapIndexed { ix, elt -> "${ix + 1} - $elt" }.joinToString("\n").message(role)

fun systemSteps(content: () -> List<String>): ChatCompletionRequestMessage = steps(system, content)

fun assistantSteps(content: () -> List<String>): ChatCompletionRequestMessage =
  steps(assistant, content)

fun userSteps(content: () -> List<String>): ChatCompletionRequestMessage = steps(user, content)

fun writeSequenceOf(
  content: String,
  prefix: String = "Step",
  role: ChatCompletionRole = assistant
): ChatCompletionRequestMessage =
  """
            Write a sequence of $content in the following format:
            $prefix 1 - ...
            $prefix 2 - ...
            ...
            $prefix N - ...
        """
    .trimIndent()
    .message(role)

fun code(
  code: String,
  delimiter: Delimiter?,
  name: String? = null,
  role: ChatCompletionRole = assistant
): ChatCompletionRequestMessage =
  """
    ${name ?: ""}
    ${delimiter?.start() ?: ""}
    $code
    ${delimiter?.end() ?: ""}
    """
    .trimIndent()
    .message(role)

enum class Delimiter {
  ThreeBackticks,
  ThreeQuotes;

  fun text(): String =
    when (this) {
      ThreeBackticks -> "triple backticks"
      ThreeQuotes -> "triple quotes"
    }

  fun start() =
    when (this) {
      ThreeBackticks -> "```"
      ThreeQuotes -> "\"\"\""
    }

  fun end() =
    when (this) {
      ThreeBackticks -> "```"
      ThreeQuotes -> "\"\"\""
    }
}
