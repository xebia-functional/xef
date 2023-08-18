package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable data class MeaningOfLife(val mainTheories: List<String>)

suspend fun main() {
  OpenAI.conversation {
    val meaningOfLife: MeaningOfLife =
      prompt("What are the main theories about the meaning of life")
    println("There are several theories about the meaning of life:\n ${meaningOfLife.mainTheories}")
  }
}
