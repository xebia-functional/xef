package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.prompt.ToolCallStrategy
import kotlinx.serialization.Serializable

@Serializable
data class Planets(
  @Description("A list of ALL the planets in the solar system") val planets: List<Planet>
)

suspend fun main() {
  val result = llama3_8b<SolarSystemPlanet>("Your favorite planet")
  println(result)
}

suspend inline fun <reified A> llama3_8b(
  prompt: String,
  config: Config = Config(baseUrl = "http://localhost:11434/v1/"),
  api: Chat = OpenAI(config = config, logRequests = true).chat,
): A =
  AI(
    prompt = prompt,
    config = config,
    api = api,
    model = CreateChatCompletionRequestModel.Custom("gemma:2b"),
    toolCallStrategy = ToolCallStrategy.InferJsonFromStringResponse
  )
