package com.xebia.functional.xef.conversation.contexts

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.llm.fromEnvironment
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.store.LocalVectorStore
import kotlinx.serialization.Serializable

@Serializable
data class Recommendation(
  @Description("The location") val location: String,
  @Description("The weather forecast") val weather: String,
  @Description(
    "The recommended clothing to wear with this kind of weather, min 50 words, required not blank"
  )
  val recommendation: String
)

suspend fun main() {
  val model = StandardModel(CreateChatCompletionRequestModel.gpt_4_1106_preview)
  val question =
    Prompt(model) { +user("Based on this weather, what do you recommend I should wear?") }
  val conversation = Conversation(LocalVectorStore(fromEnvironment(::EmbeddingsApi)))
  val search = Search(model = model, scope = conversation)
  conversation.addContext(search("Weather in Cádiz, Spain"))
  val recommendation: Recommendation = AI.chat(question, conversation = conversation)
  println(recommendation)
}
