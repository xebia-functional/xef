package com.xebia.functional.openai.models.ext.moderation.create

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface CreateModerationRequestInput {

  @Serializable @JvmInline value class StringValue(val v: String) : CreateModerationRequestInput

  @Serializable
  @JvmInline
  value class StringArrayValue(val v: List<String>) : CreateModerationRequestInput
}
