package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.AI
import com.xebia.functional.xef.conversation.Description
import kotlinx.serialization.Serializable

@Serializable
sealed class Response {

  @Serializable
  @Description("This case is chosen when the response is not a city")
  data class SomeOtherInformation(val info: String) : Response()

  @Description("This case is chosen when the response is a city")
  @Serializable
  data class City(val name: String) : Response()
}

suspend fun main() {
  val city = AI<Response>("What is the name of the capital of France?")
  println(city) // City(name=Paris)

  val other = AI<Response>("How many moons does Mars have?")
  println(other) // SomeOtherInformation(info=Mars has 2 moons.)
}
