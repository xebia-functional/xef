package com.xebia.functional.auto

import kotlinx.serialization.Serializable

@Serializable
data class Movie(val title: String, val genre: String, val director: String)

suspend fun main() {
    val movie: Movie = ai("Inception movie genre and director.")
    println("The movie ${movie.title} is a ${movie.genre} film directed by ${movie.director}.")
}
