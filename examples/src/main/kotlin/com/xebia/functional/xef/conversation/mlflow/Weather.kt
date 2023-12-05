package com.xebia.functional.xef.conversation.mlflow

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.mlflow.client.mlflowGatewayConfig
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.store.LocalVectorStore

suspend fun main() {

  // For this example you need:
  // 1. A SerpApi key to run this example and put in the environment variable SERP_API_KEY
  // https://serpapi.com/
  //
  // 2. A MLflow Gateway server with the following configuration:
  //
  //  routes:
  //  - name: chat
  //    route_type: llm/v1/chat
  //    model:
  //      provider: openai
  //      name: gpt-3.5-turbo
  //      config:
  //        openai_api_key: $OPENAI_API_KEY
  //
  //  - name: embeddings
  //    route_type: llm/v1/embeddings
  //    model:
  //      provider: openai
  //      name: text-embedding-ada-002
  //      config:
  //        openai_api_key: $OPENAI_API_KEY
  //
  // https://mlflow.org/docs/latest/llms/gateway/guides/index.html

  val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

  val httpClientConfig = mlflowGatewayConfig()

  val chatApi = ChatApi(httpClientConfig = httpClientConfig)

  val question =
    Prompt(StandardModel(model)) {
      +user("Knowing this forecast, what clothes do you recommend I should wear?")
    }

  Conversation(store = LocalVectorStore(EmbeddingsApi(httpClientConfig = httpClientConfig))) {
    val search = Search(model = StandardModel(model), scope = this, chatApi = chatApi)
    addContext(search("Weather in Cádiz, Spain"))
    val answer = promptMessage(question, chatApi)
    println(answer)
  }
}
