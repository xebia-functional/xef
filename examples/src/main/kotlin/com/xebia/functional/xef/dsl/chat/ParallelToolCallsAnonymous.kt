package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.AI
import com.xebia.functional.xef.AIConfig
import com.xebia.functional.xef.AIEvent
import com.xebia.functional.xef.Tool
import com.xebia.functional.xef.conversation.Description
import kotlinx.coroutines.flow.Flow

suspend fun ballLocationInfoFromLastCupTriedImpl(input: Int): String {
  val tip = if (input < ballCupLocation) "higher" else "lower"
  val recommendedCup =
    if (input < ballCupLocation) (input + 1)..ballCupLocation else ballCupLocation until input
  return "The ball is not under cup number $input. Try a cup with a $tip number. We recommend trying cup ${recommendedCup.random()}, ${recommendedCup.random()}, ${recommendedCup.random()} next"
}

fun lookUnderCupNumberImpl(cupNumber: Int): String =
  if (cupNumber == ballCupLocation)
    "You found the ball at $ballCupLocation's cup and it's red and shiny."
  else
    "Nothing found under cup number $cupNumber. Use the ballLocationInfoFromLastCupTried tool to get tips as to where it may be sending the last cup number you tried to find the ball."

suspend fun main() {
  val revealedSecret: Flow<AIEvent<RevealedSecret>> =
    AI(
      prompt = "Where is the ball? use the available tools to find out.",
      config =
        AIConfig(
          tools =
            listOf(
              Tool.suspend(
                "ballLocationInfoFromLastCupTried",
                Description("Get a tip on where the ball is based on the last cup number tried.")
              ) { lastTried: Int ->
                ballLocationInfoFromLastCupTriedImpl(lastTried)
              },
              Tool("lookUnderCupNumber", Description("Look under a cup to find the ball.")) {
                cupNumber: Int ->
                lookUnderCupNumberImpl(cupNumber)
              }
            )
        )
    )
  revealedSecret.collect { it.debugPrint() }
}
