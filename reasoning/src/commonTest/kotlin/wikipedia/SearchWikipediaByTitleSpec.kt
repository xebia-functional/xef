package wikipedia

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.LocalVectorStore
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import mocks.TestEmbeddings
import mocks.TestModel

class SearchWikipediaByTitleSpec :
  StringSpec({
    "Content should be the same searching by title" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val model = TestModel(modelType = ModelType.ADA, name = "fake-model")

      val scope = Conversation(LocalVectorStore(TestEmbeddings()), conversationId = conversationId)

      val search = TestSearchWikipediaByTitle(model = model, scope = scope)

      val response = search.invoke("List of bones of the human skeleton".encodeURLQueryComponent())

      val contentExpected =
        "Content: The human skeleton of an adult consists of around 206 bones, depending on the counting of sternum (which may alternatively be included as the manubrium, body of sternum, and the xiphoid process). It is composed of 270 bones at the time of birth, but later decreases to 206: 80 bones in the axial skeleton and 126 bones in the appendicular skeleton. 172 of 206 bones are part of a pair and the remaining 34 are unpaired. Many small accessory bones, such as sesamoid bones, are not included in this.\n\n"

      response shouldBe contentExpected
    }

    "Tittle should be the same searching by title" {
      val client = TestWikipediaByTitleAndPageIdClient()

      val response =
        client.searchByTitle(
          TestWikipediaByTitleAndPageIdClient.SearchDataByTitle(
            "List of bones of the human skeleton".encodeURLQueryComponent()
          )
        )

      val titleExpected = "List of bones of the human skeleton"

      response.title shouldBe titleExpected
    }

    "PageId should be the same searching by title" {
      val client = TestWikipediaByTitleAndPageIdClient()

      val response =
        client.searchByTitle(
          TestWikipediaByTitleAndPageIdClient.SearchDataByTitle(
            "List of bones of the human skeleton".encodeURLQueryComponent()
          )
        )

      val pageIDExpected = 242621

      response.pageId shouldBe pageIDExpected
    }
  })
