package serpapi

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.LocalVectorStore
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import mocks.TestEmbeddings
import mocks.TestModel

class SerpApiClientSpec :
  StringSpec({
    "content should be the same" {
      val conversationId = ConversationId(UUID.generateUUID().toString())

      val model = TestModel(modelType = ModelType.ADA, name = "fake-model")

      val scope = Conversation(LocalVectorStore(TestEmbeddings()), conversationId = conversationId)

      val search = TestSearch(model = model, scope = scope)

      val response = search.invoke("number of Leonardo di Caprio's girlfriends")

      val contentExpected =
        "Content: Gigi Hadid · Camila Morrone · Camila Morrone · Camila Morrone · Nina Agdal · Rihanna · Rihanna · Kelly Rohrbach."

      response shouldBe contentExpected
    }

    "Tittle should be the same at index 1" {
      val client = TestSerpApiClient()

      val response =
        client.search(TestSerpApiClient.SearchData("number of Leonardo di Caprio's girlfriends"))

      val titleExpected = "Leonardo DiCaprio's Dating History: Gisele, Blake Lively, ..."

      response.searchResults[1].title shouldBe titleExpected
    }

    "Source should be the same at index 2" {
      val client = TestSerpApiClient()

      val response =
        client.search(TestSerpApiClient.SearchData("number of Leonardo di Caprio's girlfriends"))

      val sourceExpected =
        "https://pagesix.com/article/leonardo-dicaprios-full-dating-history-all-of-his-ex-girlfriends/"

      response.searchResults[2].source shouldBe sourceExpected
    }
  })
