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

class SearchWikipediaSpec :
  StringSpec({
    "Size should be the same" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val model = TestModel(modelType = ModelType.ADA, name = "fake-model")

      val scope = Conversation(LocalVectorStore(TestEmbeddings()), conversationId = conversationId)

      val search = TestSearchWikipedia(model = model, scope = scope)

      val response =
        search.invoke("number of human bones in the human body".encodeURLQueryComponent())

      val contentExpected = "Content: to around 206 <span class=\"searchmatch\">bones</span> by adulthood after some <span class=\"searchmatch\">bones</span> get fused together. <span class=\"searchmatch\">The</span> <span class=\"searchmatch\">bone</span> mass <span class=\"searchmatch\">in</span> <span class=\"searchmatch\">the</span> skeleton makes up about 14% <span class=\"searchmatch\">of</span> <span class=\"searchmatch\">the</span> total <span class=\"searchmatch\">body</span> weight (ca. 10\u201311\u00a0kg"

      response shouldBe contentExpected
    }

    "Tittle should be the same at index 0" {
      val client = TestWikipediaClient()

      val response =
        client.search(
          TestWikipediaClient.TestSearchData(
            "number of human bones in the human body".encodeURLQueryComponent()
          )
        )

      val titleExpected = "Composition of the human body"

      response.searchResults.searches[0].title shouldBe titleExpected
    }

    "PageId should be the same at index 1" {
      val client = TestWikipediaClient()

      val response =
        client.search(
          TestWikipediaClient.TestSearchData(
            "number of human bones in the human body".encodeURLQueryComponent()
          )
        )

      val pageIDExpected = 168848

      response.searchResults.searches[1].pageId shouldBe pageIDExpected
    }
  })
