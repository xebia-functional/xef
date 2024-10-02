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
      ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage(
        ChatCompletionRequestAssistantMessage(
          role = ChatCompletionRequestAssistantMessage.Role.assistant,
          content = ChatCompletionRequestAssistantMessageContent.CaseString(value)
        )
      )

    fun user(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage(
        ChatCompletionRequestUserMessage(
          role = ChatCompletionRequestUserMessage.Role.user,
          content = ChatCompletionRequestUserMessageContent.CaseString(value)
        )
      )

    fun tool(toolCallId: String, value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.CaseChatCompletionRequestToolMessage(
        ChatCompletionRequestToolMessage(
          role = ChatCompletionRequestToolMessage.Role.tool,
          content = ChatCompletionRequestToolMessageContent.CaseString(value),
          toolCallId = toolCallId
        )
      )

    fun system(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage(
        ChatCompletionRequestSystemMessage(
          role = ChatCompletionRequestSystemMessage.Role.system,
          content = ChatCompletionRequestSystemMessageContent.CaseString(value)
        )
      )

    fun image(url: String, text: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage(
        ChatCompletionRequestUserMessage(
          role = ChatCompletionRequestUserMessage.Role.user,
          content =
            ChatCompletionRequestUserMessageContent
              .CaseChatCompletionRequestUserMessageContentParts(
                listOf(
                  ChatCompletionRequestUserMessageContentPart
                    .CaseChatCompletionRequestMessageContentPartImage(
                      ChatCompletionRequestMessageContentPartImage(
                        type = ChatCompletionRequestMessageContentPartImage.Type.image_url,
                        imageUrl = ChatCompletionRequestMessageContentPartImageImageUrl(url)
                      )
                    ),
                  ChatCompletionRequestUserMessageContentPart
                    .CaseChatCompletionRequestMessageContentPartText(
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
    is ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage ->
      when (val content = value.content) {
        is ChatCompletionRequestUserMessageContent.CaseString -> content.value
        is ChatCompletionRequestUserMessageContent.CaseChatCompletionRequestUserMessageContentParts ->
          content.value.joinToString {
            when (it) {
              is ChatCompletionRequestUserMessageContentPart.CaseChatCompletionRequestMessageContentPartImage ->
                it.value.imageUrl.url
              is ChatCompletionRequestUserMessageContentPart.CaseChatCompletionRequestMessageContentPartText ->
                it.value.text
            }
          }
      }
    is ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage ->
      when (val content = value.content) {
        is ChatCompletionRequestAssistantMessageContent.CaseString -> content.value
        is ChatCompletionRequestAssistantMessageContent.CaseChatCompletionRequestAssistantMessageContentParts ->
          content.value.joinToString {
            when (it) {
              is ChatCompletionRequestAssistantMessageContentPart.CaseChatCompletionRequestMessageContentPartText ->
                it.value.text
              is ChatCompletionRequestAssistantMessageContentPart.CaseChatCompletionRequestMessageContentPartRefusal ->
                it.value.refusal
            }
          }
        null -> ""
      }
    is ChatCompletionRequestMessage.CaseChatCompletionRequestToolMessage ->
      when (val content = value.content) {
        is ChatCompletionRequestToolMessageContent.CaseString -> content.value
        is ChatCompletionRequestToolMessageContent.CaseChatCompletionRequestToolMessageContentParts ->
          content.value.joinToString {
            when (it) {
              is ChatCompletionRequestToolMessageContentPart.CaseChatCompletionRequestMessageContentPartText ->
                it.value.text
            }
          }
      }
    is ChatCompletionRequestMessage.CaseChatCompletionRequestFunctionMessage -> value.content ?: ""
    is ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage ->
      when (val content = value.content) {
        is ChatCompletionRequestSystemMessageContent.CaseString -> content.value
        is ChatCompletionRequestSystemMessageContent.CaseChatCompletionRequestSystemMessageContentParts ->
          content.value.joinToString {
            when (it) {
              is ChatCompletionRequestSystemMessageContentPart.CaseChatCompletionRequestMessageContentPartText ->
                it.value.text
            }
          }
      }
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
