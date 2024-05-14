package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.prompt.ToolCallStrategy

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
    model = CreateChatCompletionRequestModel.Custom("llama3:8b"),
    toolCallStrategy = ToolCallStrategy.InferJsonFromStringResponse
  )
