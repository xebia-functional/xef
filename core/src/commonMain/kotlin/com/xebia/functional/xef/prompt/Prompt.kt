package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.prompt.templates.user
import kotlin.jvm.JvmOverloads
import kotlinx.serialization.Serializable

/**
 * A Prompt is a serializable list of messages and its configuration. The messages may involve
 * different roles.
 */
@Serializable
data class Prompt
@JvmOverloads
constructor(
  val messages: List<Message>,
  val functions: List<CFunction> = emptyList(),
  val configuration: PromptConfiguration = PromptConfiguration.DEFAULTS
) {

  constructor(value: String) : this(listOf(user(value)), emptyList())

  constructor(
    value: String,
    configuration: PromptConfiguration
  ) : this(listOf(user(value)), emptyList(), configuration)

  companion object {
    operator fun invoke(block: PromptBuilder.() -> Unit): Prompt =
      PromptBuilder().apply { block() }.build()
  }
}
