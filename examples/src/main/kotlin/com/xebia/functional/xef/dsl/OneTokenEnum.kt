package com.xebia.functional.xef.dsl

import com.xebia.functional.xef.AI
import kotlinx.serialization.Serializable

@Serializable
enum class Sentiment {
  positive,
  negative,
  neutral;

  companion object : AI<Sentiment> by AI.enum()
}

suspend fun main() {
  val sentiment = Sentiment("I love Xef!")
  println(sentiment) // positive
}
