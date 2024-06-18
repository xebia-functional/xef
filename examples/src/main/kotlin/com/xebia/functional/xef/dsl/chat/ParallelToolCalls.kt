package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.AI
import com.xebia.functional.xef.AIConfig
import com.xebia.functional.xef.AIEvent
import com.xebia.functional.xef.Tool
import com.xebia.functional.xef.conversation.Description
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

val ballCupLocation = 47

suspend fun ballLocationInfoFromLastCupTried(input: Int): String {
  return when {
    input < ballCupLocation -> "${(input..ballCupLocation).random()} may have the ball."
    input > ballCupLocation -> "${(ballCupLocation..input).random()} may have the ball."
    else -> "The ball is under cup number $input."
  }
}

fun lookUnderCupNumber(cupNumber: Int): String =
  if (cupNumber == ballCupLocation)
    "You found the ball at $ballCupLocation's cup and it's red and shiny."
  else
    "Nothing found under cup number $cupNumber. Use the ballLocationInfoFromLastCupTried tool to get tips as to where it may be sending the last cup number you tried to find the ball."

@Serializable data class RevealedSecret(val secret: String)

suspend fun main() {
  val revealedSecret: Flow<AIEvent<RevealedSecret>> =
    AI(
      prompt = "Where is the ball? use the available tools to find out.",
      config =
        AIConfig(
          tools =
            listOf(
              Tool(
                ::ballLocationInfoFromLastCupTried,
                Description(
                  "Get a tip on where the ball is based on the last cup number tried. Request this tool in parallel with different cup numbers to get multiple tips."
                )
              ),
              Tool(::lookUnderCupNumber, Description("Look under a cup to find the ball."))
            )
        )
    )
  revealedSecret.collect { it.debugPrint() }
}
