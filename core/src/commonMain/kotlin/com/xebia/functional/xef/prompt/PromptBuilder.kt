package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import kotlin.jvm.JvmSynthetic

interface PromptBuilder {
  val items: MutableList<ChatCompletionRequestMessage>

  fun preprocess(elements: List<ChatCompletionRequestMessage>): List<ChatCompletionRequestMessage>

  fun build(): Prompt

  @JvmSynthetic
  operator fun Prompt.unaryPlus() {
    +messages
  }

  @JvmSynthetic
  operator fun ChatCompletionRequestMessage.unaryPlus() {
    addMessage(this)
  }

  private fun ChatCompletionRequestMessage.addContent(
    message: ChatCompletionRequestMessage
  ): ChatCompletionRequestMessage {
    val content = "${contentAsString()}\n${message.contentAsString()}"
    return when (completionRole()) {
      ChatCompletionRole.Supported.system -> system(content)
      ChatCompletionRole.Supported.user -> user(content)
      ChatCompletionRole.Supported.assistant -> assistant(content)
      ChatCompletionRole.Supported.tool -> error("Tool role is not supported")
      ChatCompletionRole.Supported.function -> error("Function role is not supported")
      is ChatCompletionRole.Custom -> error("Custom roles are not supported")
    }
  }

  @JvmSynthetic
  operator fun List<ChatCompletionRequestMessage>.unaryPlus() {
    addMessages(this)
  }

  fun addMessage(message: ChatCompletionRequestMessage): PromptBuilder = apply {
    val lastMessageWithSameRole: ChatCompletionRequestMessage? =
      items.lastMessageWithSameRole(message)
    if (lastMessageWithSameRole != null) {
      val messageUpdated = lastMessageWithSameRole.addContent(message)
      items.remove(lastMessageWithSameRole)
      items.add(messageUpdated)
    } else {
      items.add(message)
    }
  }

  fun addMessages(messages: List<ChatCompletionRequestMessage>): PromptBuilder = apply {
    val last = items.removeLastOrNull()
    items.addAll(((last?.let { listOf(it) } ?: emptyList()) + messages).flatten())
  }

  private fun List<ChatCompletionRequestMessage>.flatten(): List<ChatCompletionRequestMessage> =
    fold(mutableListOf()) { acc, message ->
      val lastMessageWithSameRole: ChatCompletionRequestMessage? =
        acc.lastMessageWithSameRole(message)
      if (lastMessageWithSameRole != null) {
        val messageUpdated = lastMessageWithSameRole.addContent(message)
        acc.remove(lastMessageWithSameRole)
        acc.add(messageUpdated)
      } else {
        acc.add(message)
      }
      acc
    }

  private fun List<ChatCompletionRequestMessage>.lastMessageWithSameRole(
    message: ChatCompletionRequestMessage
  ): ChatCompletionRequestMessage? =
    lastOrNull()?.let { if (it.completionRole() == message.completionRole()) it else null }

  companion object {

    operator fun invoke(
      model: CreateChatCompletionRequestModel,
      functions: List<FunctionObject>,
      configuration: PromptConfiguration
    ): PlatformPromptBuilder = PlatformPromptBuilder.create(model, functions, configuration)

    fun assistant(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.First(
        ChatCompletionRequestAssistantMessage(
          role = ChatCompletionRequestAssistantMessage.Role.assistant,
          content = value
        )
      )

    fun user(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.Fifth(
        ChatCompletionRequestUserMessage(
          role = ChatCompletionRequestUserMessage.Role.user,
          content = ChatCompletionRequestUserMessageContent.First(value)
        )
      )

    fun system(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.Third(
        ChatCompletionRequestSystemMessage(
          role = ChatCompletionRequestSystemMessage.Role.system,
          content = value
        )
      )

    fun image(url: String, text: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.Fifth(
        ChatCompletionRequestUserMessage(
          role = ChatCompletionRequestUserMessage.Role.user,
          content =
            ChatCompletionRequestUserMessageContent.Second(
              listOf(
                ChatCompletionRequestMessageContentPart.First(
                  ChatCompletionRequestMessageContentPartImage(
                    type = ChatCompletionRequestMessageContentPartImage.Type.image_url,
                    imageUrl = ChatCompletionRequestMessageContentPartImageImageUrl(url)
                  )
                ),
                ChatCompletionRequestMessageContentPart.Second(
                  ChatCompletionRequestMessageContentPartText(
                    type = ChatCompletionRequestMessageContentPartText.Type.text,
                    text = text
                  )
                )
              )
            )
        )
      )
  }
}

fun ChatCompletionRequestMessage.contentAsString(): String =
  when (this) {
    is ChatCompletionRequestMessage.Fifth ->
      when (val content = value.content) {
        is ChatCompletionRequestUserMessageContent.First -> content.value
        is ChatCompletionRequestUserMessageContent.Second ->
          content.value.joinToString {
            when (it) {
              is ChatCompletionRequestMessageContentPart.First -> it.value.imageUrl.url
              is ChatCompletionRequestMessageContentPart.Second -> it.value.text
            }
          }
      }
    is ChatCompletionRequestMessage.First -> value.content ?: ""
    is ChatCompletionRequestMessage.Fourth -> value.content
    is ChatCompletionRequestMessage.Second -> value.content ?: ""
    is ChatCompletionRequestMessage.Third -> value.content
  }

internal fun ChatCompletionRequestMessage.completionRole(): ChatCompletionRole =
  when (this) {
    /* this conversion is needed because we don't have a hierarchy for nested roles */
    is ChatCompletionRequestMessage.Fifth -> ChatCompletionRole.valueOf(value.role.name)
    is ChatCompletionRequestMessage.First -> ChatCompletionRole.valueOf(value.role.name)
    is ChatCompletionRequestMessage.Fourth -> ChatCompletionRole.valueOf(value.role.name)
    is ChatCompletionRequestMessage.Second -> ChatCompletionRole.valueOf(value.role.name)
    is ChatCompletionRequestMessage.Third -> ChatCompletionRole.valueOf(value.role.name)
  }
