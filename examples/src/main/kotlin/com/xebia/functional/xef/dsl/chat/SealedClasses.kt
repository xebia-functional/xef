package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.AI
import com.xebia.functional.xef.AIConfig
import com.xebia.functional.xef.AIEvent
import com.xebia.functional.xef.Tool
import com.xebia.functional.xef.conversation.Description
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
sealed class Response {

  @Serializable
  @Description(
    "This case is chosen when the response is not a city and the user asks about planets. First call `informationOnPlanet` to get the information."
  )
  data class SomeOtherInformation(val info: String) : Response()

  @Description("This case is chosen when the response is a city")
  @Serializable
  data class City(val name: String) : Response()
}

fun informationOnPlanet(planet: Planet): String =
  when (planet.name) {
    "Mars" -> "It's a secret but we just discovered that Mars has 100 moons."
    else -> "I don't have information on the number of moons for ${planet.name}."
  }

suspend fun main() {
  val city = AI<Response>("What is the name of the capital of France?")
  println(city) // City(name=Paris)

  val other =
    AI<Flow<AIEvent<Response>>>(
      prompt = "How many moons does Mars have based on the recent discovery?",
      config = AIConfig(tools = listOf(Tool(::informationOnPlanet)))
    )
  other.collect { it.debugPrint() }
}
