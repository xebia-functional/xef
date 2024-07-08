package com.xebia.functional.xef.prompt.configuration

import com.xebia.functional.xef.conversation.MessagePolicy
import com.xebia.functional.xef.openapi.ChatCompletionRole
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlinx.serialization.Serializable

@Serializable
data class PromptConfiguration
@JvmOverloads
constructor(
  var maxDeserializationAttempts: Int = 3,
  var maxToolCallsPerRound: Int = 10000,
  var concurrentToolCallsPerRound: Int = 5,
  var user: String = ChatCompletionRole.User.value,
  var temperature: Double = 0.4,
  var numberOfPredictions: Int = 1,
  var maxTokens: Int = 500,
  var messagePolicy: MessagePolicy = MessagePolicy(),
  var seed: Int? = null,
) {

  fun messagePolicy(block: MessagePolicy.() -> Unit) = messagePolicy.apply { block() }

  companion object {
    @JvmField val DEFAULTS = PromptConfiguration()

    operator fun invoke(block: PromptConfiguration.() -> Unit) =
      PromptConfiguration().apply { block() }
  }
}
