package com.xebia.functional.xef.store

import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.data.TestEmbeddings
import com.xebia.functional.xef.llm.models.modelType
import com.xebia.functional.xef.llm.tokensFromMessages
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LocalVectorStoreSpec :
  StringSpec({
    val model = CreateChatCompletionRequestModel._3_5_turbo
    "memories function should return all of messages in the right order when the limit is greater than the number of stored messages" {
      val localVectorStore = LocalVectorStore(TestEmbeddings())

      val memoryData = MemoryData()

      val messages1 = memoryData.generateRandomMessages(4)
      val messages2 = memoryData.generateRandomMessages(3)

      localVectorStore.addMemories(messages1)
      localVectorStore.addMemories(messages2)

      val messages =
        localVectorStore.memories(model, memoryData.defaultConversationId, Int.MAX_VALUE)

      val messagesExpected = messages1 + messages2

      messages shouldBe messagesExpected
    }

    "memories function should return the last n messages in the right order" {
      val modelType = model.modelType()
      val localVectorStore = LocalVectorStore(TestEmbeddings())

      val memoryData = MemoryData()

      val messages1 = memoryData.generateRandomMessages(4)
      val messages2 = memoryData.generateRandomMessages(3)

      val tokensForMessages2 =
        modelType.tokensFromMessages(messages2.map { it.content.asRequestMessage() })

      localVectorStore.addMemories(messages1)
      localVectorStore.addMemories(messages2)

      val messages =
        localVectorStore.memories(model, memoryData.defaultConversationId, tokensForMessages2)

      messages shouldBe messages2
    }

    "memories function should return the last n messages in the right order for a specific conversation id" {
      val modelType = model.modelType()
      val localVectorStore = LocalVectorStore(TestEmbeddings())

      val firstId = ConversationId("first-id")
      val secondId = ConversationId("second-id")

      val memoryData = MemoryData()

      val messages1 = memoryData.generateRandomMessages(4, conversationId = firstId)
      val messages2 = memoryData.generateRandomMessages(3, conversationId = secondId)

      localVectorStore.addMemories(messages1 + messages2)

      val tokensForMessages1 =
        modelType.tokensFromMessages(messages1.map { it.content.asRequestMessage() })
      val tokensForMessages2 =
        modelType.tokensFromMessages(messages2.map { it.content.asRequestMessage() })

      val messagesFirstId = localVectorStore.memories(model, firstId, tokensForMessages1)

      val messagesSecondId = localVectorStore.memories(model, secondId, tokensForMessages2)

      messagesFirstId shouldBe messages1
      messagesSecondId shouldBe messages2
    }
  })
