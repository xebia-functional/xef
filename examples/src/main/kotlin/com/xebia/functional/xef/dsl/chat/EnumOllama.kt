package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI

suspend fun main() {
  val config = Config(baseUrl = "http://localhost:11434/v1/", supportsLogitBias = false)
  val sentiment =
    AI<Sentiment>(
      prompt = "I love Xef!",
      model = CreateChatCompletionRequestModel.Custom("orca-mini:3b"),
      config = config,
      api = OpenAI(config, logRequests = true).chat,
    )
  println(sentiment) // positive
}
