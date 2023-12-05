package com.xebia.functional.openai.models.ext.assistant

import com.xebia.functional.openai.models.RunStepDetailsMessageCreationObjectMessageCreation
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunStepDetailsMessageCreationObject(
  @SerialName(value = "message_creation")
  @Required
  val messageCreation: RunStepDetailsMessageCreationObjectMessageCreation,
  /* Always `message_creation``. */
  @SerialName(value = "type") @Required val type: Type
) : RunStepObjectStepDetails {

  constructor(
    messageCreation: RunStepDetailsMessageCreationObjectMessageCreation
  ) : this(messageCreation, Type.message_creation)

  /**
   * Always `message_creation``.
   *
   * Values: message_creation
   */
  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "message_creation") message_creation("message_creation")
  }
}
