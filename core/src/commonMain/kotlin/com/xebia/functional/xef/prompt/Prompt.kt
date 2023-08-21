package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.prompt.templates.user
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * A Prompt is a serializable list of messages and its configuration. The messages may involve
 * different roles.
 */
data class Prompt
@JvmOverloads
constructor(
  val messages: List<Message>,
  val function: CFunction? = null,
  val configuration: PromptConfiguration = PromptConfiguration.DEFAULTS
) {

  constructor(value: String) : this(listOf(user(value)), null)

  constructor(
    value: String,
    configuration: PromptConfiguration
  ) : this(listOf(user(value)), null, configuration)

  companion object {
    @JvmSynthetic
    operator fun invoke(block: PlatformPromptBuilder.() -> Unit): Prompt =
      PlatformPromptBuilder.create().apply { block() }.build()
  }
}
