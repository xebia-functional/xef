package com.xebia.functional.xef.auto

import com.xebia.functional.xef.llm.models.chat.Role
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

class PromptConfiguration(
  val maxDeserializationAttempts: Int = 3,
  val user: String = Role.USER.name,
  val temperature: Double = 0.4,
  val numberOfPredictions: Int = 1,
  val docsInContext: Int = 5,
  val memoryLimit: Int = 5,
  val minResponseTokens: Int = 500,
  val messagePolicy: MessagePolicy = MessagePolicy(),
) {
  companion object {

    class Builder {
      private var maxDeserializationAttempts: Int = 3
      private var user: String = Role.USER.name
      private var temperature: Double = 0.4
      private var numberOfPredictions: Int = 1
      private var docsInContext: Int = 20
      private var minResponseTokens: Int = 500
      private var memoryLimit: Int = 5
      private var messagePolicy: MessagePolicy = MessagePolicy()

      fun maxDeserializationAttempts(maxDeserializationAttempts: Int) = apply {
        this.maxDeserializationAttempts = maxDeserializationAttempts
      }

      fun user(user: String) = apply { this.user = user }

      fun temperature(temperature: Double) = apply { this.temperature = temperature }

      fun numberOfPredictions(numberOfPredictions: Int) = apply {
        this.numberOfPredictions = numberOfPredictions
      }

      fun docsInContext(docsInContext: Int) = apply { this.docsInContext = docsInContext }

      fun minResponseTokens(minResponseTokens: Int) = apply {
        this.minResponseTokens = minResponseTokens
      }

      fun memoryLimit(memoryLimit: Int) = apply { this.memoryLimit = memoryLimit }

      fun messagePolicy(messagePolicy: MessagePolicy) = apply { this.messagePolicy = messagePolicy }

      fun build() =
        PromptConfiguration(
          maxDeserializationAttempts = maxDeserializationAttempts,
          user = user,
          temperature = temperature,
          numberOfPredictions = numberOfPredictions,
          docsInContext = docsInContext,
          memoryLimit = memoryLimit,
          minResponseTokens = minResponseTokens,
          messagePolicy = messagePolicy,
        )
    }

    @JvmName("build")
    operator fun invoke(block: Builder.() -> Unit) = Builder().apply(block).build()

    @JvmField val DEFAULTS = PromptConfiguration()
  }
}

/**
 * The [MessagePolicy] encapsulates the message selection policy for sending to the server. Allows
 * defining the percentages of historical and contextual messages to include in the final list.
 *
 * @property historyPercent Percentage of historical messages
 * @property contextPercent Percentage of context messages
 */
class MessagePolicy(
  val historyPercent: Int = 50,
  val historyPaddingTokens: Int = 100,
  val contextPercent: Int = 50,
)
