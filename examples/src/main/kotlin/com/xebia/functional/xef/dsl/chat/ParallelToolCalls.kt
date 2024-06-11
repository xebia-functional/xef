package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.AI
import com.xebia.functional.xef.AIConfig
import com.xebia.functional.xef.AIEvent
import com.xebia.functional.xef.Tool
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

fun ballLocationInfo(input: String): String = "The ball is in the 47 cup."

fun lookUnderCupNumber(cupNumber: Int): String =
  if (cupNumber == 47) "You found the ball and it's red and shiny."
  else
    "Nothing found under cup number $cupNumber. Use the ballLocationInfo tool to find which cup the ball is under."

@Serializable data class RevealedSecret(val secret: String)

suspend fun main() {
  val revealedSecret: Flow<AIEvent<RevealedSecret>> =
    AI(
      prompt = "Where is the ball? use the available tools to find out.",
      config =
        AIConfig(tools = listOf(Tool.toolOf(::ballLocationInfo), Tool.toolOf(::lookUnderCupNumber)))
    )
  revealedSecret.collect {
    when (it) {
      // emoji for start is: 🚀
      AIEvent.Start -> println("🚀 Starting...")
      is AIEvent.Result -> println("🎉 ${it.value.secret}")
      is AIEvent.ToolExecutionRequest ->
        println("🔧 Executing tool: ${it.tool.function.name} with input: ${it.input}")
      is AIEvent.ToolExecutionResponse ->
        println("🔨 Tool response: ${it.tool.function.name} resulted in: ${it.output}")
      is AIEvent.Stop -> {
        println("🛑 Stopping...")
        println("📊 Usage: ${it.usage}")
      }
    }
  }
}
