package com.xebia.functional.xef.auto

import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.llm.AIClient
import com.xebia.functional.xef.llm.LLMModel
import com.xebia.functional.xef.llm.openai.OpenAIClient
import com.xebia.functional.xef.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import kotlin.jvm.JvmStatic
import kotlin.time.ExperimentalTime

data class AIRuntime<A, C : AIClient>(
  val client: C,
  val embeddings: Embeddings,
  val runtime: suspend (block: AI<A>) -> A
) {
  companion object {

    @JvmStatic fun <A> defaults(): AIRuntime<A, out AIClient> = openAI(null)

    @OptIn(ExperimentalTime::class)
    @JvmStatic
    fun <A> openAI(config: OpenAIConfig? = null): AIRuntime<A, OpenAIClient> {
      val openAIConfig =
        config
          ?: OpenAIConfig(
            logging = LoggingConfig(logLevel = LogLevel.None, logger = Logger.Empty),
            token =
              requireNotNull(getenv("OPENAI_TOKEN")) { "OpenAI Token missing from environment." },
          )
      val openAI = OpenAI(openAIConfig)
      val client = OpenAIClient(openAI)
      val embeddings = OpenAIEmbeddings(client)
      return AIRuntime(client, embeddings) { block ->
        client.use { openAiClient ->
          val vectorStore = LocalVectorStore(embeddings)
          val scope =
            CoreAIScope(
              defaultModel = LLMModel.GPT_3_5_TURBO_16K,
              defaultSerializationModel = LLMModel.GPT_3_5_TURBO_FUNCTIONS,
              aiClient = openAiClient,
              context = vectorStore,
              embeddings = embeddings
            )
          block(scope)
        }
      }
    }
  }
}
