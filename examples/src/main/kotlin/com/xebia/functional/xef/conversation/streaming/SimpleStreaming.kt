package com.xebia.functional.xef.conversation.streaming

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.fromEnvironment
import com.xebia.functional.xef.metrics.LogsMetric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.LocalVectorStore

suspend fun main() {
  val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo)

  val scope = Conversation(LocalVectorStore(fromEnvironment(::EmbeddingsApi)), LogsMetric())

  scope
    .promptStreaming(
      Prompt(model, "Create a 1000 word essay about Mars"),
    )
    .collect { element -> print(element) }
}
