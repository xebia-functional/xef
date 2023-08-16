package com.xebia.functional.xef.auto

import com.xebia.functional.xef.llm.models.chat.Role
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlinx.serialization.Serializable

@Serializable
data class PromptConfiguration
@JvmOverloads
constructor(
  val maxDeserializationAttempts: Int = 3,
  val user: String = Role.USER.name,
  val temperature: Double = 0.4,
  val numberOfPredictions: Int = 1,
  val docsInContext: Int = 5,
  val minResponseTokens: Int = 500,
  val messagePolicy: MessagePolicy = MessagePolicy(),
) {
  companion object {
    @JvmField val DEFAULTS = PromptConfiguration()
  }
}
