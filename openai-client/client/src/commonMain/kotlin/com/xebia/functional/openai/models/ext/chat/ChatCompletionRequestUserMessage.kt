package com.xebia.functional.openai.models.ext.chat

import com.xebia.functional.openai.models.ChatCompletionRole
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param content
 * @param role The role of the messages author, in this case `user`.
 */
@Serializable
data class ChatCompletionRequestUserMessage(
  @SerialName(value = "content")
  @Required
  val content: List<ChatCompletionRequestUserMessageContent>,

  /* The role of the messages author, in this case `user`. */
  @SerialName(value = "role") @Required val role: Role = Role.user
) : ChatCompletionRequestMessage {

  /**
   * The role of the messages author, in this case `user`.
   *
   * Values: user
   */
  @Serializable
  enum class Role(val value: String) {
    @SerialName(value = "user") user("user");

    val asRole: ChatCompletionRole = ChatCompletionRole.user
  }
}
