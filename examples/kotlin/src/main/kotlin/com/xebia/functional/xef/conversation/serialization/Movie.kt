package com.xebia.functional.xef.conversation.serialization

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.metrics.LogsMetric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.LocalVectorStore
import kotlinx.serialization.Serializable

@Serializable data class Movie(val title: String, val genre: String, val director: String)

suspend fun main() {
  // This example contemplate the case of calling OpenAI directly or
  // calling through a local Xef Server instance.
  // To run the example with the Xef Server, you can execute the following commands:
  //  - # docker compose-up server/docker/postgresql
  //  - # ./gradlew server
  val openAI = OpenAI.fromEnvironment()
  //    val openAI = OpenAI(host = "http://localhost:8081/")
  val model = openAI.DEFAULT_SERIALIZATION

  val scope = Conversation(LocalVectorStore(openAI.DEFAULT_EMBEDDING), LogsMetric())

  model
    .prompt(
      Prompt("Please provide a movie title, genre and director for the Inception movie"),
      scope,
      Movie.serializer()
    )
    .let { movie ->
      println("The movie ${movie.title} is a ${movie.genre} film directed by ${movie.director}.")
    }
}
