package serpapi

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SerpApiClientSpec :
  StringSpec({
    /*"""
    | When we are using a OpenAI scope conversation
    | the memories should have the correct size in the vector store
    | for the conversationId generated inside the conversation
    """ {
        OpenAI.conversation {
            val model = TestModel(modelType = ModelType.ADA, name = "fake-model")

            promptMessage(prompt = Prompt("question 1"), model = model)

            promptMessage(prompt = Prompt("question 2"), model = model)

            val memories = store.memories(conversationId!!, 10000)

            memories.size shouldBe 4
        }
    }*/

    "Tittle should be the same at index 0" {
      val client = TestSerpApiClient()

      val response = client.search(TestSerpApiClient.SearchData("number of Leonardo di Caprio's girlfriends"))

      response.searchResults[0].title shouldBe "Leonardo DiCaprio's Dating History: Each Girlfriend In His ..."
    }

    "Document should be the same at index 1" {
      val client = TestSerpApiClient()

      val response = client.search(TestSerpApiClient.SearchData("number of Leonardo di Caprio's girlfriends"))

      response.searchResults[1].document shouldBe "Take a look back at Leonardo DiCaprio's many high-profile relationships through the years, from Bridget Hall to Camila Morrone — photos."
    }

    "Source should be the same at index 2" {
      val client = TestSerpApiClient()

      val response = client.search(TestSerpApiClient.SearchData("number of Leonardo di Caprio's girlfriends"))

      response.searchResults[2].source shouldBe "https://pagesix.com/article/leonardo-dicaprios-full-dating-history-all-of-his-ex-girlfriends/"
    }
  })
