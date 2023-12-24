package com.xebia.functional.xef.dsl

import com.xebia.functional.xef.AI
import kotlinx.serialization.Serializable

@Serializable
sealed class Response {

  @Serializable data class City(val name: String) : Response()

  @Serializable data class Country(val name: String) : Response()

  @Serializable data class Continent(val name: String) : Response()
}

suspend fun main() {
  val response = AI<Response>("Capital of France?")
  println(response) // City(name=Paris)
}
