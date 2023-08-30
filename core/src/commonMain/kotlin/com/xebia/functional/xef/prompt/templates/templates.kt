package com.xebia.functional.xef.prompt.templates

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.prompt.PlatformPromptBuilder
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.message

fun system(context: String): Message = context.message(Role.SYSTEM)

fun assistant(context: String): Message = context.message(Role.ASSISTANT)

fun user(context: String): Message = context.message(Role.USER)

inline fun <reified A> system(data: A): Message = data.message(Role.SYSTEM)

inline fun <reified A> assistant(data: A): Message = data.message(Role.ASSISTANT)

inline fun <reified A> user(data: A): Message = data.message(Role.USER)

class StepsMessageBuilder : PlatformPromptBuilder() {

  override fun preprocess(elements: List<Message>): List<Message> =
    elements.mapIndexed { ix, elt -> "${ix + 1} - ${elt.content}".message(elt.role) }
}

fun steps(inside: StepsMessageBuilder.() -> Unit): Prompt =
  StepsMessageBuilder().apply { inside() }.build()

fun writeSequenceOf(
  content: String,
  prefix: String = "Step",
  role: Role = Role.ASSISTANT
): Message =
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
  role: Role = Role.ASSISTANT
): Message =
  """
    ${name ?: "" }
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
