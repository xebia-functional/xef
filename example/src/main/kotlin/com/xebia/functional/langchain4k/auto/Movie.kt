package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Movie(val title: String, val genre: String, val director: String)

suspend fun main() {
    ai {
        val movie: Movie = ai("Inception movie genre and director.")
        println("The movie ${movie.title} is a ${movie.genre} film directed by ${movie.director}.")
    }.getOrElse { println(it) }
}
