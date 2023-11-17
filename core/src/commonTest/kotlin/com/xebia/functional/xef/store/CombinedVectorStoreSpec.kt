package com.xebia.functional.xef.store

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateFineTuneRequestModel
import com.xebia.functional.xef.data.TestEmbeddings
import com.xebia.functional.xef.llm.tokensFromMessages
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.coroutines.CoroutineContext

class CombinedVectorStoreSpec :
  StringSpec({
    val model = StandardModel(CreateFineTuneRequestModel.ada)
    "memories function should return all of messages combined in the right order" {
      val memoryData = MemoryData()

      val topMessages = memoryData.generateRandomMessages(4, append = "top")
      val bottomMessages = memoryData.generateRandomMessages(4, append = "bottom")

      val combinedVectorStore = topMessages.combine(coroutineContext, bottomMessages)

      val messages =
        combinedVectorStore.memories(model, memoryData.defaultConversationId, Int.MAX_VALUE)

      val messagesExpected = topMessages + bottomMessages

      messages shouldBe messagesExpected
    }

    "memories function should return the last n combined messages in the right order" {
      val modelType = model.modelType()
      val memoryData = MemoryData()

      val topMessages = memoryData.generateRandomMessages(4, append = "top")
      val bottomMessages = memoryData.generateRandomMessages(4, append = "bottom")

      val combinedVectorStore = topMessages.combine(coroutineContext, bottomMessages)

      val lastTwoMessages = topMessages.takeLast(2 * 2).map { it.content.asRequestMessage() }
      val tokensForMessages =
        modelType.tokensFromMessages(
          lastTwoMessages + bottomMessages.map { it.content.asRequestMessage() }
        )

      val messages =
        combinedVectorStore.memories(model, memoryData.defaultConversationId, tokensForMessages)

      val messagesExpected = topMessages.takeLast(2 * 2) + bottomMessages

      messages shouldBe messagesExpected
    }

    "memories function should return the messages with common conversation id combined in the right order" {
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
        (topMessages + commonTopMessages).combine(
          coroutineContext,
          bottomMessages + commonBottomMessages
        )

      val messages = combinedVectorStore.memories(model, commonId, Int.MAX_VALUE)

      val messagesExpected = commonTopMessages + commonBottomMessages

      messages shouldBe messagesExpected
    }

    "adding messages to a combined vector store" {
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
        (topMessages + commonTopMessages).combine(
          coroutineContext,
          bottomMessages + commonBottomMessages
        )

      val newCommonMessages =
        memoryData.generateRandomMessages(4, append = "new", conversationId = commonId)
      combinedVectorStore.addMemories(newCommonMessages)

      val tokensForNewCommonMessages =
        model
          .modelType()
          .tokensFromMessages(newCommonMessages.map { it.content.asRequestMessage() })

      combinedVectorStore.memories(model, commonId, tokensForNewCommonMessages) shouldBe
        newCommonMessages
    }
  })

suspend fun List<Memory>.combine(
  context: CoroutineContext,
  bottomMessages: List<Memory>
): CombinedVectorStore {
  val top = LocalVectorStore(TestEmbeddings(context))
  top.addMemories(this)

  val bottom = LocalVectorStore(TestEmbeddings(context))
  bottom.addMemories(bottomMessages)

  return CombinedVectorStore(top, bottom)
}
