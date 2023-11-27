package com.xebia.functional.openai.models.ext.chat

import com.xebia.functional.openai.models.ChatCompletionRole
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param content The contents of the system message.
 * @param role The role of the messages author, in this case `system`.
 */
@Serializable
data class ChatCompletionRequestSystemMessage(

  /* The contents of the system message. */
  @SerialName(value = "content") @Required val content: String?,

  /* The role of the messages author, in this case `system`. */
  @SerialName(value = "role") @Required val role: Role
) : ChatCompletionRequestMessage {

  constructor(content: String?) : this(content, Role.system)

  /**
   * The role of the messages author, in this case `system`.
   *
   * Values: system
   */
  @Serializable
  enum class Role(val value: String) {
    @SerialName(value = "system") system("system");

    val asRole: ChatCompletionRole = ChatCompletionRole.system
  }
}
