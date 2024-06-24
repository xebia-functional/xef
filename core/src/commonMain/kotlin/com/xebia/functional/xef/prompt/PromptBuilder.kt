package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import io.github.nomisrev.openapi.*
import io.github.nomisrev.openapi.ChatCompletionRequestMessage
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
      ChatCompletionRole.System -> system(content)
      ChatCompletionRole.User -> user(content)
      ChatCompletionRole.Assistant -> assistant(content)
      ChatCompletionRole.Tool -> error("Tool role is not supported")
      ChatCompletionRole.Function -> error("Function role is not supported")
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
      model: CreateChatCompletionRequest.Model,
      functions: List<FunctionObject>,
      configuration: PromptConfiguration
    ): PlatformPromptBuilder = PlatformPromptBuilder.create(model, functions, configuration)

    fun assistant(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage(
        ChatCompletionRequestAssistantMessage(
          role = ChatCompletionRequestAssistantMessage.Role.Assistant,
          content = value
        )
      )

    fun user(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage(
        ChatCompletionRequestUserMessage(
          role = ChatCompletionRequestUserMessage.Role.User,
          content = ChatCompletionRequestUserMessage.Content.CaseString(value)
        )
      )

    fun system(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage(
        ChatCompletionRequestSystemMessage(
          role = ChatCompletionRequestSystemMessage.Role.System,
          content = value
        )
      )

    fun image(url: String, text: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage(
        ChatCompletionRequestUserMessage(
          role = ChatCompletionRequestUserMessage.Role.User,
          content =
            ChatCompletionRequestUserMessage.Content.CaseChatCompletionRequestMessageContentParts(
              listOf(
                ChatCompletionRequestMessageContentPart
                  .CaseChatCompletionRequestMessageContentPartImage(
                    ChatCompletionRequestMessageContentPartImage(
                      type = ChatCompletionRequestMessageContentPartImage.Type.ImageUrl,
                      imageUrl = ChatCompletionRequestMessageContentPartImage.ImageUrl(url)
                    )
                  ),
                ChatCompletionRequestMessageContentPart
                  .CaseChatCompletionRequestMessageContentPartText(
                    ChatCompletionRequestMessageContentPartText(
                      type = ChatCompletionRequestMessageContentPartText.Type.Text,
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
    is ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage ->
      when (val content = value.content) {
        is ChatCompletionRequestUserMessage.Content.CaseString -> content.value
        is ChatCompletionRequestUserMessage.Content.CaseChatCompletionRequestMessageContentParts ->
          content.value.joinToString {
            when (it) {
              is ChatCompletionRequestMessageContentPart.CaseChatCompletionRequestMessageContentPartImage ->
                it.value.imageUrl.url
              is ChatCompletionRequestMessageContentPart.CaseChatCompletionRequestMessageContentPartText ->
                it.value.text
            }
          }
      }
    is ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage -> value.content ?: ""
    is ChatCompletionRequestMessage.CaseChatCompletionRequestToolMessage -> value.content
    is ChatCompletionRequestMessage.CaseChatCompletionRequestFunctionMessage -> value.content ?: ""
    is ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage -> value.content
  }

internal fun ChatCompletionRequestMessage.completionRole(): ChatCompletionRole =
  when (this) {
    /* this conversion is needed because we don't have a hierarchy for nested roles */
    is ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage ->
      ChatCompletionRole.valueOf(value.role.name)
    is ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage ->
      ChatCompletionRole.valueOf(value.role.name)
    is ChatCompletionRequestMessage.CaseChatCompletionRequestToolMessage ->
      ChatCompletionRole.valueOf(value.role.name)
    is ChatCompletionRequestMessage.CaseChatCompletionRequestFunctionMessage ->
      ChatCompletionRole.valueOf(value.role.name)
    is ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage ->
      ChatCompletionRole.valueOf(value.role.name)
  }
