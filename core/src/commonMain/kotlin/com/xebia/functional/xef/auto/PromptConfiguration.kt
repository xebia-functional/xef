package com.xebia.functional.xef.auto

import com.xebia.functional.xef.llm.models.chat.Role
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

class PromptConfiguration(
  val maxDeserializationAttempts: Int = 3,
  val user: String = Role.USER.name,
  val temperature: Double = 0.4,
  val numberOfPredictions: Int = 1,
  val docsInContext: Int = 20,
  val minResponseTokens: Int = 500,
) {
  companion object {

    class Builder {
      private var maxDeserializationAttempts: Int = 3
      private var user: String = Role.USER.name
      private var temperature: Double = 0.4
      private var numberOfPredictions: Int = 1
      private var docsInContext: Int = 20
      private var minResponseTokens: Int = 500

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

      fun build() =
        PromptConfiguration(
          maxDeserializationAttempts,
          user,
          temperature,
          numberOfPredictions,
          docsInContext,
          minResponseTokens
        )
    }

    @JvmName("build")
    operator fun invoke(block: Builder.() -> Unit) = Builder().apply(block).build()

    @JvmField val DEFAULTS = PromptConfiguration()
  }
}
