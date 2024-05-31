package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.AI
import kotlinx.serialization.Serializable

@Serializable
sealed class Response {

  @Serializable data class City(val name: String) : Response()

  @Serializable data class Country(val name: String) : Response()

  @Serializable data class Continent(val name: String) : Response()
}

suspend fun main() {
  val response = AI<Response>("What is the capital of France?")
  println(response) // City(name=Paris)
}
