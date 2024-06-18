package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.AI
import com.xebia.functional.xef.AIConfig
import com.xebia.functional.xef.AIEvent
import com.xebia.functional.xef.Tool
import kotlinx.coroutines.flow.Flow

suspend fun main() {
  val result = AI<Flow<String>>("List of planets in the solar system")
  result.collect(::print)

  val info =
    AI<Flow<AIEvent<String>>>(
      prompt = "How many moons in Mars?. Use the available tools to find out.",
      config = AIConfig(tools = listOf(Tool(::informationOnPlanet)))
    )

  info.collect {
    when (it) {
      is AIEvent.Result -> println(it.value)
      else -> it.debugPrint()
    }
  }
}
