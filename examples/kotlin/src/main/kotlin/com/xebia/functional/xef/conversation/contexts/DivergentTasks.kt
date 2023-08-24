package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import com.xebia.functional.xef.reasoning.serpapi.Search
import kotlinx.serialization.Serializable

@Serializable data class NumberOfMedicalNeedlesInWorld(val numberOfNeedles: Long)

suspend fun main() {
  OpenAI.conversation {
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, this)
    addContext(search("Estimate amount of medical needles in the world"))
    val needlesInWorld: NumberOfMedicalNeedlesInWorld =
      prompt("Provide the number of medical needles in the world")
    println("Needles in world: ${needlesInWorld.numberOfNeedles}")
  }
}
