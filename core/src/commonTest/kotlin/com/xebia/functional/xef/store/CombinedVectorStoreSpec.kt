package com.xebia.functional.xef.store

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.data.TestEmbeddings
import com.xebia.functional.xef.data.TestChatModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CombinedVectorStoreSpec :
  StringSpec({
    "memories function should return all of messages combined in the right order" {
      val model = TestChatModel(modelType = ModelType.ADA)

      val memoryData = MemoryData()

      val topMessages = memoryData.generateRandomMessages(4, append = "top")
      val bottomMessages = memoryData.generateRandomMessages(4, append = "bottom")

      val combinedVectorStore = topMessages.combine(bottomMessages)

      val messages =
        combinedVectorStore.memories(model, memoryData.defaultConversationId, Int.MAX_VALUE)

      val messagesExpected = topMessages + bottomMessages

      messages shouldBe messagesExpected
    }

    "memories function should return the last n combined messages in the right order" {
      val model = TestChatModel(modelType = ModelType.ADA)

      val memoryData = MemoryData()

      val topMessages = memoryData.generateRandomMessages(4, append = "top")
      val bottomMessages = memoryData.generateRandomMessages(4, append = "bottom")

      val combinedVectorStore = topMessages.combine(bottomMessages)

      val tokensForLast2TopMessages =
        model.tokensFromMessages(topMessages.takeLast(2 * 2).map { it.content.asRequestMessage() })
      val tokensForBottomMessages = model.tokensFromMessages(bottomMessages.map { it.content.asRequestMessage() })

      val messages =
        combinedVectorStore.memories(
          model,
          memoryData.defaultConversationId,
          tokensForLast2TopMessages + tokensForBottomMessages
        )

      val messagesExpected = topMessages.takeLast(2 * 2) + bottomMessages

      messages shouldBe messagesExpected
    }

    "memories function should return the messages with common conversation id combined in the right order" {
      val model = TestChatModel(modelType = ModelType.ADA)

      val memoryData = MemoryData()

      val topId = ConversationId("top-id")
      val bottomId = ConversationId("bottom-id")
      val commonId = ConversationId("common-id")

      val topMessages = memoryData.generateRandomMessages(4, append = "top", conversationId = topId)
      val commonTopMessages =
        memoryData.generateRandomMessages(4, append = "common-top", conversationId = commonId)

      val bottomMessages =
        memoryData.generateRandomMessages(4, append = "bottom", conversationId = bottomId)
      val commonBottomMessages =
        memoryData.generateRandomMessages(4, append = "common-bottom", conversationId = commonId)

      val combinedVectorStore =
        (topMessages + commonTopMessages).combine(bottomMessages + commonBottomMessages)

      val messages = combinedVectorStore.memories(model, commonId, Int.MAX_VALUE)

      val messagesExpected = commonTopMessages + commonBottomMessages

      messages shouldBe messagesExpected
    }

    "adding messages to a combined vector store" {
      val model = TestChatModel(modelType = ModelType.ADA)

      val memoryData = MemoryData()

      val topId = ConversationId("top-id")
      val bottomId = ConversationId("bottom-id")
      val commonId = ConversationId("common-id")

      val topMessages = memoryData.generateRandomMessages(4, append = "top", conversationId = topId)
      val commonTopMessages =
        memoryData.generateRandomMessages(4, append = "common-top", conversationId = commonId)

      val bottomMessages =
        memoryData.generateRandomMessages(4, append = "bottom", conversationId = bottomId)
      val commonBottomMessages =
        memoryData.generateRandomMessages(4, append = "common-bottom", conversationId = commonId)

      val combinedVectorStore =
        (topMessages + commonTopMessages).combine(bottomMessages + commonBottomMessages)

      val newCommonMessages =
        memoryData.generateRandomMessages(4, append = "new", conversationId = commonId)
      combinedVectorStore.addMemories(newCommonMessages)

      val tokensForNewCommonMessages =
        model.tokensFromMessages(newCommonMessages.map { it.content.asRequestMessage() })

      combinedVectorStore.memories(model, commonId, tokensForNewCommonMessages) shouldBe
        newCommonMessages
    }
  })

suspend fun List<Memory>.combine(bottomMessages: List<Memory>): CombinedVectorStore {
  val top = LocalVectorStore(TestEmbeddings())
  top.addMemories(this)

  val bottom = LocalVectorStore(TestEmbeddings())
  bottom.addMemories(bottomMessages)

  return CombinedVectorStore(top, bottom)
}
