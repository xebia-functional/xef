package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.AI
import kotlinx.serialization.Serializable

@Serializable
enum class Sentiment {
  positive,
  negative,
  neutral
}

suspend fun main() {
  val sentiment = AI<Sentiment>("I love Xef!")
  println(sentiment) // positive
}
