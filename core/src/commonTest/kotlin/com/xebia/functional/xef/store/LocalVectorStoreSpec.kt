package com.xebia.functional.xef.store

import com.xebia.functional.xef.data.TestEmbeddings
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LocalVectorStoreSpec :
  StringSpec({
    "memories function should return all of messages in the right order when the limit is greater than the number of stored messages" {
      val localVectorStore = LocalVectorStore(TestEmbeddings())

      val messages1 = generateRandomMessages(4, startTimestamp = 1000)
      val messages2 = generateRandomMessages(3, startTimestamp = 2000)

      localVectorStore.addMemories(messages1)
      localVectorStore.addMemories(messages2)

      val messages = localVectorStore.memories(defaultConversationId, Int.MAX_VALUE)

      val messagesExpected = messages1 + messages2

      messages shouldBe messagesExpected
    }

    "memories function should return the last n messages in the right order" {
      val localVectorStore = LocalVectorStore(TestEmbeddings())

      val messages1 = generateRandomMessages(4, startTimestamp = 1000)
      val messages2 = generateRandomMessages(3, startTimestamp = 2000)

      val tokensForMessages2 = messages2.sumOf { calculateTokens(it.content) }

      localVectorStore.addMemories(messages1)
      localVectorStore.addMemories(messages2)

      val messages = localVectorStore.memories(defaultConversationId, tokensForMessages2)

      messages shouldBe messages2
    }

    "memories function should return the last n messages in the right order for a specific conversation id" {
      val localVectorStore = LocalVectorStore(TestEmbeddings())

      val firstId = ConversationId("first-id")
      val secondId = ConversationId("second-id")

      val messages1 = generateRandomMessages(4, conversationId = firstId, startTimestamp = 1000)
      val messages2 = generateRandomMessages(3, conversationId = secondId, startTimestamp = 2000)

      localVectorStore.addMemories(messages1 + messages2)

      val tokensForMessages1 = messages1.sumOf { calculateTokens(it.content) }
      val tokensForMessages2 = messages2.sumOf { calculateTokens(it.content) }

      val messagesFirstId = localVectorStore.memories(firstId, tokensForMessages1)

      val messagesSecondId = localVectorStore.memories(secondId, tokensForMessages2)

      messagesFirstId shouldBe messages1
      messagesSecondId shouldBe messages2
    }
  })
