package com.xebia.functional.openai.models.ext.chat

import com.xebia.functional.openai.models.ChatCompletionRole
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param role The role of the messages author, in this case `tool`.
 * @param content The contents of the tool message.
 * @param toolCallId Tool call that this message is responding to.
 */
@Serializable
data class ChatCompletionRequestToolMessage(

  /* The contents of the tool message. */
  @SerialName(value = "content") @Required val content: String?,

  /* Tool call that this message is responding to. */
  @SerialName(value = "tool_call_id") @Required val toolCallId: String,

  /* The role of the messages author, in this case `tool`. */
  @SerialName(value = "role") @Required val role: Role = Role.tool
) : ChatCompletionRequestMessage {

  /**
   * The role of the messages author, in this case `tool`.
   *
   * Values: tool
   */
  @Serializable
  enum class Role(val value: String) {
    @SerialName(value = "tool") tool("tool");

    val asRole: ChatCompletionRole = ChatCompletionRole.tool
  }
}
