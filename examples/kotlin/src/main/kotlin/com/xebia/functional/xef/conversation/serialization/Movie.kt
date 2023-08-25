package com.xebia.functional.xef.conversation.serialization

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable data class Movie(val title: String, val genre: String, val director: String)

suspend fun main() {
  OpenAI.conversation {
    val movie: Movie =
      prompt("Please provide a movie title, genre and director for the Inception movie")
    println("The movie ${movie.title} is a ${movie.genre} film directed by ${movie.director}.")
  }
}
