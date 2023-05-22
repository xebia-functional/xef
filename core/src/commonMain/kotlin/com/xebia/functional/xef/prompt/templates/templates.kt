package com.xebia.functional.xef.prompt.templates

import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder
import com.xebia.functional.xef.prompt.prompt

fun youAre(you: String, talkingTo: String): Prompt =
  "You are a $you talking with a $talkingTo".prompt()

class StepsBuilder : PromptBuilder() {
  override fun preprocess(elements: List<Prompt>): List<Prompt> =
    elements.mapIndexed { ix, elt -> Prompt("${ix + 1} - ${elt.message}") }
}

fun steps(inside: PromptBuilder.() -> Unit): Prompt = StepsBuilder().apply { inside() }.build()

fun writeSequenceOf(content: String): Prompt =
  """
            Write a sequence of $content in the following format:
            Step 1 - ...
            Step 2 - ...
            ...
            Step N - ...
        """
    .trimIndent()
    .prompt()

fun writeListOf(content: String): Prompt =
  """
            Write a list of $content in the following format:
            1. ...
            2. ...
            n. ...
        """
    .trimIndent()
    .prompt()

fun code(code: String, delimiter: Delimiter?, name: String? = null): Prompt =
  """
    ${name ?: "" }
    ${delimiter?.start() ?: ""}
    $code
    ${delimiter?.end() ?: ""}
    """
    .trimIndent()
    .prompt()

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
