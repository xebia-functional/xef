package com.xebia.functional.xef.prompt.configuration

import com.xebia.functional.xef.conversation.MessagePolicy
import com.xebia.functional.xef.llm.models.chat.Role
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlinx.serialization.Serializable

@Serializable
data class PromptConfiguration
@JvmOverloads
constructor(
  var maxDeserializationAttempts: Int = 3,
  var user: String = Role.USER.name,
  var temperature: Double = 0.4,
  var numberOfPredictions: Int = 1,
  var docsInContext: Int = 5,
  var minResponseTokens: Int = 500,
  var messagePolicy: MessagePolicy = MessagePolicy(),
) {

  fun messagePolicy(block: MessagePolicy.() -> Unit) = messagePolicy.apply { block() }

  companion object {
    @JvmField val DEFAULTS = PromptConfiguration()

    operator fun invoke(block: PromptConfiguration.() -> Unit) =
      PromptConfiguration().apply { block() }
  }
}
