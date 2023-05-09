package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.prompt
import com.xebia.functional.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class Movie(val title: String, val genre: String, val director: String)

suspend fun main() {
    prompt {
        val movie: Movie = prompt("Inception movie genre and director.")
        println("The movie ${movie.title} is a ${movie.genre} film directed by ${movie.director}.")
    }.getOrElse { println(it) }
}
