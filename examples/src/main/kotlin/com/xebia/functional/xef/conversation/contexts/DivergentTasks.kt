package com.xebia.functional.xef.conversation.contexts

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.serpapi.Search
import kotlinx.serialization.Serializable

@Serializable data class NumberOfMedicalNeedlesInWorld(val numberOfNeedles: Long)

suspend fun main() {
  Conversation {
    val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo_16k_0613)
    val search = Search(model = model, scope = this)
    addContext(search("Estimate amount of medical needles in the world"))
    val needlesInWorld: NumberOfMedicalNeedlesInWorld =
      prompt(Prompt(model) { +user("Provide the number of medical needles in the world") })
    println("Needles in world: ${needlesInWorld.numberOfNeedles}")
  }
}
