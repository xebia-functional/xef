package com.xebia.functional.xef.vectorstores

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LocalVectorStoreSpec :
    StringSpec({
        "memories function should return all of messages in the right order when the limit is greater than the number of stored messages" {
            val localVectorStore = LocalVectorStore(FakeEmbeddings())

            val messages1 = generateRandomMessages(4)
            val messages2 = generateRandomMessages(3)

            localVectorStore.addMemories(messages1)
            localVectorStore.addMemories(messages2)

            val messages = localVectorStore.memories(defaultConversationId, Int.MAX_VALUE)

            val messagesExpected = messages1 + messages2

            messages shouldBe messagesExpected
        }

        "memories function should return the last n messages in the right order" {
            val localVectorStore = LocalVectorStore(FakeEmbeddings())

            val limit = 3 * 2 // 3 couples of messages

            val messages1 = generateRandomMessages(4)
            val messages2 = generateRandomMessages(3)

            localVectorStore.addMemories(messages1)
            localVectorStore.addMemories(messages2)

            val messages = localVectorStore.memories(defaultConversationId, limit)

            val messagesExpected = (messages1 + messages2).takeLast(limit)

            messages shouldBe messagesExpected
        }

        "memories function should return the last n messages in the right order for a specific conversation id" {
            val localVectorStore = LocalVectorStore(FakeEmbeddings())

            val limit = 3 * 2

            val firstId = ConversationId("first-id")
            val secondId = ConversationId("second-id")

            val messages1 = generateRandomMessages(4, conversationId = firstId)
            val messages2 = generateRandomMessages(3, conversationId = secondId)

            localVectorStore.addMemories(messages1 + messages2)

            val messagesFirstId = localVectorStore.memories(firstId, limit)
            val messagesFirstIdExpected = messages1.takeLast(limit)

            val messagesSecondId = localVectorStore.memories(secondId, limit)
            val messagesSecondIdExpected = messages2.takeLast(limit)

            messagesFirstId shouldBe messagesFirstIdExpected
            messagesSecondId shouldBe messagesSecondIdExpected
        }

    })
