package com.xebia.functional.openai.models.ext.chat

import com.xebia.functional.openai.models.ChatCompletionRole
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param role The role of the messages author, in this case `function`.
 * @param content The return value from the function call, to return to the model.
 * @param name The name of the function to call.
 */
@Serializable
@Deprecated(message = "This schema is deprecated.")
data class ChatCompletionRequestFunctionMessage(

  /* The return value from the function call, to return to the model. */
  @SerialName(value = "content") @Required val content: String?,

  /* The name of the function to call. */
  @SerialName(value = "name") @Required val name: String,

  /* The role of the messages author, in this case `function`. */
  @SerialName(value = "role") @Required val role: Role = Role.function
) : ChatCompletionRequestMessage {

  /**
   * The role of the messages author, in this case `function`.
   *
   * Values: function
   */
  @Serializable
  enum class Role(val value: String) {
    @SerialName(value = "function") function("function");

    val asRole: ChatCompletionRole = ChatCompletionRole.function
  }
}
