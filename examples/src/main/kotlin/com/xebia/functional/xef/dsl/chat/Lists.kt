package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.AI
import kotlinx.serialization.Serializable

@Serializable data class Planet(val name: String)

suspend fun main() {
  val response = AI<List<Planet>>("Planets in the solar system")
  println(response)
}
