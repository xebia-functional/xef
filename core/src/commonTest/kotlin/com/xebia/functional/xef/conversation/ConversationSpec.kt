package com.xebia.functional.xef.conversation

import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.data.TestChatApi
import com.xebia.functional.xef.data.TestEmbeddings
import com.xebia.functional.xef.llm.models.modelType
import com.xebia.functional.xef.llm.promptMessage
import com.xebia.functional.xef.llm.promptMessages
import com.xebia.functional.xef.llm.tokensFromMessages
import com.xebia.functional.xef.metrics.LogsMetric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.assistant
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.system
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.prompt.contentAsString
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.LocalVectorStore
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class ConversationSpec :
  StringSpec({
    "memories should have the correct size in the vector store" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val chatApi = TestChatApi()
      val model = CreateChatCompletionRequestModel.gpt_4

      val scope =
        Conversation(
          LocalVectorStore(TestEmbeddings()),
          LogsMetric(),
          conversationId = conversationId
        )

      val vectorStore = scope.store

      chatApi.promptMessages(prompt = Prompt(model, "question 1"), scope = scope)

      chatApi.promptMessages(prompt = Prompt(model, "question 2"), scope = scope)

      val memories = vectorStore.memories(model, conversationId, 10000)

      memories.size shouldBe 4
    }

    """"
      | GPT_3_5_TURBO model has 4097 max context length
      | when the number of token in the conversation is greater than
      | the space allotted for the message history in the prompt configuration
      | the number of messages in the request must have fewer messages than
      | the total number of messages in the conversation
      |""" {
      val messages = generateRandomMessages(50, 40, 60)
      val conversationId = ConversationId(UUID.generateUUID().toString())
      val scope =
        Conversation(
          LocalVectorStore(TestEmbeddings()),
          LogsMetric(),
          conversationId = conversationId
        )
      val vectorStore = scope.store

      val chatApi = TestChatApi(responses = messages)
      val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

      val totalTokens =
        model.modelType().tokensFromMessages(messages.flatMap(::chatCompletionRequestMessages))

      messages.forEach { message ->
        chatApi.promptMessages(prompt = Prompt(model, message.key), scope = scope)
      }

      val lastRequest = chatApi.requests.last()

      val memories = vectorStore.memories(model, conversationId, totalTokens)

      // The messages in the request doesn't contain the message response
      val messagesSizePlusMessageResponse = lastRequest.messages.size + 1

      messagesSizePlusMessageResponse shouldBeLessThan memories.size
    }

    """"
      | GPT Turbo 16K model has 16388 max context length
      | when the number of token in the conversation is less than
      | the space allotted for the message history in the prompt configuration
      | the request must send all messages in the conversation
      |""" {
      val messages = generateRandomMessages(50, 40, 60)
      val conversationId = ConversationId(UUID.generateUUID().toString())
      val scope =
        Conversation(
          LocalVectorStore(TestEmbeddings()),
          LogsMetric(),
          conversationId = conversationId
        )
      val vectorStore = scope.store

      val chatApi = TestChatApi(responses = messages)
      val model = CreateChatCompletionRequestModel.gpt_3_5_turbo_16k

      val totalTokens =
        model.modelType().tokensFromMessages(messages.flatMap(::chatCompletionRequestMessages))

      messages.forEach { message ->
        chatApi.promptMessages(prompt = Prompt(model, message.key), scope = scope)
      }

      val lastRequest = chatApi.requests.last()

      val memories = vectorStore.memories(model, conversationId, totalTokens)

      // The messages in the request doesn't contain the message response
      val messagesSizePlusMessageResponse = lastRequest.messages.size + 1

      messagesSizePlusMessageResponse shouldBe memories.size
    }

    "the scope's store should contains all the messages" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val chatApi = TestChatApi()
      val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

      val scope =
        Conversation(
          LocalVectorStore(TestEmbeddings()),
          LogsMetric(),
          conversationId = conversationId
        )

      val vectorStore = scope.store

      val firstPrompt =
        Prompt(model) {
          +system("question 1")
          +user("question 2")
        }

      val firstResponse = chatApi.promptMessage(prompt = firstPrompt, scope = scope)
      val aiFirstMessage =
        ChatCompletionRequestAssistantMessage(
          role = ChatCompletionRequestAssistantMessage.Role.assistant,
          content = ChatCompletionRequestAssistantMessageContent.CaseString(firstResponse)
        )

      val secondPrompt = Prompt(model) { +user("question 3") }

      val secondResponse = chatApi.promptMessage(prompt = secondPrompt, scope = scope)
      val aiSecondMessage =
        ChatCompletionRequestAssistantMessage(
          role = ChatCompletionRequestAssistantMessage.Role.assistant,
          content = ChatCompletionRequestAssistantMessageContent.CaseString(secondResponse)
        )

      val thirdPrompt =
        Prompt(model) {
          +system("question 4")
          +user("question 5")
        }

      val thirdResponse = chatApi.promptMessage(prompt = thirdPrompt, scope = scope)
      val aiThirdMessage =
        ChatCompletionRequestAssistantMessage(
          role = ChatCompletionRequestAssistantMessage.Role.assistant,
          content = ChatCompletionRequestAssistantMessageContent.CaseString(secondResponse)
        )

      val memories = vectorStore.memories(model, conversationId, 10000)
      val expectedMessages: List<String> =
        (firstPrompt.messages +
            ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage(aiFirstMessage) +
            secondPrompt.messages +
            ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage(
              aiSecondMessage
            ) +
            thirdPrompt.messages +
            ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage(aiThirdMessage))
          .map { it.contentAsString() }
      val actualMessages = memories.map { it.content.asRequestMessage().contentAsString() }
      actualMessages shouldBe expectedMessages
    }

    "when using 2 different scopes with the same conversationId, the index of the memories should be in order" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val chatApi = TestChatApi()
      val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

      val vectorStore = LocalVectorStore(TestEmbeddings())

      val scope1 = Conversation(vectorStore, LogsMetric(), conversationId = conversationId)

      val firstPrompt =
        Prompt(model) {
          +user("question in scope 1")
          +assistant("answer in scope 1")
        }

      chatApi.promptMessages(prompt = firstPrompt, scope = scope1)

      val scope2 = Conversation(vectorStore, LogsMetric(), conversationId = conversationId)

      val secondPrompt =
        Prompt(model) {
          +user("question in scope 2")
          +assistant("answer in scope 2")
        }

      chatApi.promptMessages(prompt = secondPrompt, scope = scope2)

      val memories = vectorStore.memories(model, conversationId, 10000)

      memories.map { it.index } shouldBe listOf(1, 2, 3, 4, 5, 6)
    }

    "when using MessagesToHistory.ALL policy, the scope's store should contains all messages" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val chatApi = TestChatApi()
      val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

      val vectorStore = LocalVectorStore(TestEmbeddings())

      val scope = Conversation(vectorStore, LogsMetric(), conversationId = conversationId)

      val prompt =
        Prompt(model) {
            +system("system message")
            +user("question in scope 1")
          }
          .copy(
            configuration =
              PromptConfiguration {
                messagePolicy { addMessagesToConversation = MessagesToHistory.ALL }
              }
          )

      chatApi.promptMessages(prompt = prompt, scope = scope)

      val messagesStored = scope.store.memories(model, conversationId, Int.MAX_VALUE)

      messagesStored.size shouldBe 3
    }

    "when using MessagesToHistory.ONLY_SYSTEM_MESSAGES policy, the scope's store should contains only system messages" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val chatApi = TestChatApi()
      val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

      val vectorStore = LocalVectorStore(TestEmbeddings())

      val scope = Conversation(vectorStore, LogsMetric(), conversationId = conversationId)

      val prompt =
        Prompt(model) {
            +system("system message")
            +user("question in scope 1")
          }
          .copy(
            configuration =
              PromptConfiguration {
                messagePolicy { addMessagesToConversation = MessagesToHistory.ONLY_SYSTEM_MESSAGES }
              }
          )

      chatApi.promptMessages(prompt = prompt, scope = scope)

      val messagesStored = scope.store.memories(model, conversationId, Int.MAX_VALUE)

      println(messagesStored)

      messagesStored.filter { it.content.role == ChatCompletionRole.system } shouldBe messagesStored
    }

    "when using MessagesToHistory.NOT_SYSTEM_MESSAGES policy, the scope's store shouldn't contains system messages" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val chatApi = TestChatApi()
      val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

      val vectorStore = LocalVectorStore(TestEmbeddings())

      val scope = Conversation(vectorStore, LogsMetric(), conversationId = conversationId)

      val prompt =
        Prompt(model) {
            +system("system message")
            +user("question in scope 1")
          }
          .copy(
            configuration =
              PromptConfiguration {
                messagePolicy { addMessagesToConversation = MessagesToHistory.NOT_SYSTEM_MESSAGES }
              }
          )

      chatApi.promptMessages(prompt = prompt, scope = scope)

      val messagesStored = scope.store.memories(model, conversationId, Int.MAX_VALUE)

      messagesStored.filter { it.content.role != ChatCompletionRole.system } shouldBe messagesStored
    }

    "when using MessagesToHistory.NONE policy, the scope's store shouldn't contains messages" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val chatApi = TestChatApi()
      val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

      val vectorStore = LocalVectorStore(TestEmbeddings())

      val scope = Conversation(vectorStore, LogsMetric(), conversationId = conversationId)

      val prompt =
        Prompt(model) {
            +system("system message")
            +user("question in scope 1")
          }
          .copy(
            configuration =
              PromptConfiguration {
                messagePolicy { addMessagesToConversation = MessagesToHistory.NONE }
              }
          )

      chatApi.promptMessages(prompt = prompt, scope = scope)

      val messagesStored =
        scope.store.memories(model, conversationId, Int.MAX_VALUE).filter {
          it.content.role == ChatCompletionRole.system
        }

      messagesStored.size shouldBe 0
    }

    "when using MessagesFromHistory.ALL policy, the request should contains the previous messages in the conversation" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val chatApi = TestChatApi()
      val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

      val vectorStore = LocalVectorStore(TestEmbeddings())

      val scope = Conversation(vectorStore, LogsMetric(), conversationId = conversationId)

      val firstPrompt =
        Prompt(model) {
          +system("system message")
          +user("question in scope 1")
        }

      chatApi.promptMessages(prompt = firstPrompt, scope = scope)

      val secondPrompt =
        Prompt(model) {
            +user("question in scope 2")
            +assistant("answer in scope 2")
            +user("question in scope 3")
          }
          .copy(
            configuration =
              PromptConfiguration {
                messagePolicy { addMessagesFromConversation = MessagesFromHistory.ALL }
              }
          )

      chatApi.promptMessages(prompt = secondPrompt, scope = scope)

      chatApi.requests.last().messages.size shouldBe 6
    }

    "when using MessagesFromHistory.NONE policy, the request shouldn't contains the previous messages in the conversation" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val chatApi = TestChatApi()
      val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

      val vectorStore = LocalVectorStore(TestEmbeddings())

      val scope = Conversation(vectorStore, LogsMetric(), conversationId = conversationId)

      val firstPrompt =
        Prompt(model) {
          +system("system message")
          +user("question in scope 1")
        }

      chatApi.promptMessages(prompt = firstPrompt, scope = scope)

      val secondPrompt =
        Prompt(model) {
            +user("question in scope 2")
            +assistant("answer in scope 2")
            +user("question in scope 3")
          }
          .copy(
            configuration =
              PromptConfiguration {
                messagePolicy { addMessagesFromConversation = MessagesFromHistory.NONE }
              }
          )

      chatApi.promptMessages(prompt = secondPrompt, scope = scope)

      chatApi.requests.last().messages.size shouldBe 3
    }
  })

private fun chatCompletionRequestMessages(it: Map.Entry<String, String>) =
  listOf(
    ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage(
      ChatCompletionRequestUserMessage(
        role = ChatCompletionRequestUserMessage.Role.user,
        content = ChatCompletionRequestUserMessageContent.CaseString(it.key)
      )
    ),
    ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage(
      ChatCompletionRequestAssistantMessage(
        role = ChatCompletionRequestAssistantMessage.Role.assistant,
        content = ChatCompletionRequestAssistantMessageContent.CaseString(it.value)
      )
    ),
  )
