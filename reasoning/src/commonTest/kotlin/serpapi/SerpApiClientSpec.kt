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

      val response = search.invoke("german shepper")

      val contentExpected =
        "Content: Generally considered dogkind's finest all-purpose worker, the German Shepherd Dog is a large, agile, muscular dog of noble character and high intelligence."

      response shouldBe contentExpected
    }

    "Tittle should be the same at index 1" {
      val client = TestSerpApiClient()

      val response =
        client.search(TestSerpApiClient.SearchData("german shepper"))

      val titleExpected = "German Shepherd Dog Breed Information & Characteristics"

      response.searchResults[1].title shouldBe titleExpected
    }

    "Source should be the same at index 2" {
      val client = TestSerpApiClient()

      val response =
        client.search(TestSerpApiClient.SearchData("german shepper"))

      val sourceExpected =
        "https://www.britannica.com/animal/German-shepherd"

      response.searchResults[2].source shouldBe sourceExpected
    }
  })
