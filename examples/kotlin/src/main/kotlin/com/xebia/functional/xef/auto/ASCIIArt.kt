package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.conversation
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable data class ASCIIArt(val art: String)

suspend fun main() {
  val art: ASCIIArt = conversation { prompt("ASCII art of a cat dancing") }
  println(art)
}
