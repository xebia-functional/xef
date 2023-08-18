package com.xebia.functional.xef.store

import com.xebia.functional.xef.data.TestEmbeddings
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CombinedVectorStoreSpec :
  StringSpec({
    "memories function should return all of messages combined in the right order" {
      val topMessages = generateRandomMessages(4, append = "top", startTimestamp = 1000)
      val bottomMessages = generateRandomMessages(4, append = "bottom", startTimestamp = 2000)

      val combinedVectorStore = topMessages.combine(bottomMessages)

      val messages = combinedVectorStore.memories(defaultConversationId, Int.MAX_VALUE)

      val messagesExpected = topMessages + bottomMessages

      messages shouldBe messagesExpected
    }

    "memories function should return the last n combined messages in the right order" {
      val topMessages = generateRandomMessages(4, append = "top", startTimestamp = 1000)
      val bottomMessages = generateRandomMessages(4, append = "bottom", startTimestamp = 2000)

      val combinedVectorStore = topMessages.combine(bottomMessages)

      val tokensForLast2TopMessages =
        topMessages.takeLast(2 * 2).sumOf { calculateTokens(it.content) }
      val tokensForBottomMessages = bottomMessages.sumOf { calculateTokens(it.content) }

      val messages =
        combinedVectorStore.memories(
          defaultConversationId,
          tokensForLast2TopMessages + tokensForBottomMessages
        )

      val messagesExpected = topMessages.takeLast(2 * 2) + bottomMessages

      messages shouldBe messagesExpected
    }

    "memories function should return the messages with common conversation id combined in the right order" {
      val topId = ConversationId("top-id")
      val bottomId = ConversationId("bottom-id")
      val commonId = ConversationId("common-id")

      val topMessages =
        generateRandomMessages(4, append = "top", conversationId = topId, startTimestamp = 1000)
      val commonTopMessages =
        generateRandomMessages(
          4,
          append = "common-top",
          conversationId = commonId,
          startTimestamp = 2000
        )

      val bottomMessages =
        generateRandomMessages(
          4,
          append = "bottom",
          conversationId = bottomId,
          startTimestamp = 3000
        )
      val commonBottomMessages =
        generateRandomMessages(
          4,
          append = "common-bottom",
          conversationId = commonId,
          startTimestamp = 4000
        )

      val combinedVectorStore =
        (topMessages + commonTopMessages).combine(bottomMessages + commonBottomMessages)

      val messages = combinedVectorStore.memories(commonId, Int.MAX_VALUE)

      val messagesExpected = commonTopMessages + commonBottomMessages

      messages shouldBe messagesExpected
    }

    "adding messages to a combined vector store" {
      val topId = ConversationId("top-id")
      val bottomId = ConversationId("bottom-id")
      val commonId = ConversationId("common-id")

      val topMessages =
        generateRandomMessages(4, append = "top", conversationId = topId, startTimestamp = 1000)
      val commonTopMessages =
        generateRandomMessages(
          4,
          append = "common-top",
          conversationId = commonId,
          startTimestamp = 2000
        )

      val bottomMessages =
        generateRandomMessages(
          4,
          append = "bottom",
          conversationId = bottomId,
          startTimestamp = 3000
        )
      val commonBottomMessages =
        generateRandomMessages(
          4,
          append = "common-bottom",
          conversationId = commonId,
          startTimestamp = 4000
        )

      val combinedVectorStore =
        (topMessages + commonTopMessages).combine(bottomMessages + commonBottomMessages)

      val newCommonMessages =
        generateRandomMessages(4, append = "new", conversationId = commonId, startTimestamp = 5000)
      combinedVectorStore.addMemories(newCommonMessages)

      val tokensForNewCommonMessages = newCommonMessages.sumOf { calculateTokens(it.content) }

      combinedVectorStore.memories(commonId, tokensForNewCommonMessages) shouldBe newCommonMessages
    }
  })

suspend fun List<Memory>.combine(bottomMessages: List<Memory>): CombinedVectorStore {
  val top = LocalVectorStore(TestEmbeddings())
  top.addMemories(this)

  val bottom = LocalVectorStore(TestEmbeddings())
  bottom.addMemories(bottomMessages)

  return CombinedVectorStore(top, bottom)
}
