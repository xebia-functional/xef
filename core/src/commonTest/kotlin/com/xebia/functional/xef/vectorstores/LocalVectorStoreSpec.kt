package com.xebia.functional.xef.vectorstores

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LocalVectorStoreSpec :
    StringSpec({
        "memories function should return all of messages in the right order when the limit is greater than the number of stored messages" {
            val localVectorStore = LocalVectorStore(FakeEmbeddings())

            localVectorStore.addMemories(messages1)
            localVectorStore.addMemories(messages2)

            val messages = localVectorStore.memories(defaultConversationId, Int.MAX_VALUE)

            val messagesExpected = messages1 + messages2

            messages shouldBe messagesExpected
        }

        "memories function should return the last n messages in the right order" {
            val localVectorStore = LocalVectorStore(FakeEmbeddings())

            localVectorStore.addMemories(messages1)
            localVectorStore.addMemories(messages2)

            val messages = localVectorStore.memories(defaultConversationId, 2)

            val messagesExpected = (messages1 + messages2).takeLast(2)

            messages shouldBe messagesExpected
        }

    })
