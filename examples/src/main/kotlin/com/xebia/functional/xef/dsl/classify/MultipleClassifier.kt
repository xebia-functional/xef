package com.xebia.functional.xef.dsl.classify

import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.conversation.Description
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
enum class Sports : AI.PromptMultipleClassifier {
  @Description(
    "Football is a team sport that is played on a rectangular field with goalposts at each end. The objective of the game is to score points by moving the ball into the opposing team's goal. The team with the most points at the end of the game wins."
  )
  FOOTBALL,
  @Description(
    "The game of basketball is played with a ball and a hoop. The objective is to score points by shooting the ball through the hoop. The game is played on a rectangular court with a hoop at each end. The team with the most points at the end of the game wins."
  )
  BASKETBALL,
  @Description(
    "The game of tennis is played with a racket and a ball. The objective is to hit the ball over the net and into the opponent's court. The game is played on a rectangular court with a net at the center. The player with the most points at the end of the game wins."
  )
  VOLLEYBALL,
  @Description(
    "The game of cricket is played with a bat and a ball. The objective is to score runs by hitting the ball and running between the wickets. The game is played on a circular field with a wicket at each end. The team with the most runs at the end of the game wins."
  )
  CRICKET,
  @Description(
    "The game of chess is played on a square board with 64 squares arranged in an 8x8 grid. The objective is to checkmate the opponent's king by placing it under threat of capture. The player who checkmates the opponent's king wins the game."
  )
  CHESS;

  override fun getItems(): List<AI.Classification> = typeOf<Sports>().enumValuesName()
}

/**
 * This is a simple example of how to use the `AI.multipleClassify` function to classify a prompt
 */
suspend fun main() {

  println(AI.multipleClassify<Sports>("Sport played with a racket"))
  println(
    AI.multipleClassify<Sports>(
      input = "The game is played with a ball",
      model = CreateChatCompletionRequestModel.gpt_4o
    )
  )
}
