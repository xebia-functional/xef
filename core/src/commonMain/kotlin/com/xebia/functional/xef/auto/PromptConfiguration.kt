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
  val streamToStandardOut: Boolean = false
) {
  companion object {

    class Builder {
      private var maxDeserializationAttempts: Int = 3
      private var user: String = Role.USER.name
      private var temperature: Double = 0.4
      private var numberOfPredictions: Int = 1
      private var docsInContext: Int = 20
      private var minResponseTokens: Int = 500
      private var streamToStandardOut: Boolean = false
      private var memoryLimit: Int = 5

      fun maxDeserializationAttempts(maxDeserializationAttempts: Int) = apply {
        this.maxDeserializationAttempts = maxDeserializationAttempts
      }

      fun streamToStandardOut(streamToStandardOut: Boolean) = apply {
        this.streamToStandardOut = streamToStandardOut
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

      fun build() =
        PromptConfiguration(
          maxDeserializationAttempts = maxDeserializationAttempts,
          user = user,
          temperature = temperature,
          numberOfPredictions = numberOfPredictions,
          docsInContext = docsInContext,
          memoryLimit = memoryLimit,
          minResponseTokens = minResponseTokens,
          streamToStandardOut = streamToStandardOut,
        )
    }

    @JvmName("build")
    operator fun invoke(block: Builder.() -> Unit) = Builder().apply(block).build()

    @JvmField val DEFAULTS = PromptConfiguration()

    fun buildWithParams(docsInContext: Int, streamToStandardOut: Boolean) =
      PromptConfiguration(docsInContext = docsInContext, streamToStandardOut = streamToStandardOut)
  }
}
