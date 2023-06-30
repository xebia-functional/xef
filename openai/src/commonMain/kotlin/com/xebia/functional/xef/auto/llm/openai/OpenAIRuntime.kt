package com.xebia.functional.xef.auto.llm.openai

import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.xebia.functional.xef.auto.AIRuntime
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import kotlin.jvm.JvmStatic
import kotlin.time.ExperimentalTime
import com.xebia.functional.xef.auto.llm.openai.OpenAI as XefOpenAI

object OpenAIRuntime {
  @JvmStatic fun <A> defaults(): AIRuntime<A> = openAI(null)

  @OptIn(ExperimentalTime::class)
  @JvmStatic
  fun <A> openAI(config: OpenAIConfig? = null): AIRuntime<A> {
    val openAIConfig =
      config
        ?: OpenAIConfig(
          logging = LoggingConfig(logLevel = LogLevel.None, logger = Logger.Empty),
          token =
            requireNotNull(getenv("OPENAI_TOKEN")) { "OpenAI Token missing from environment." },
        )
    val client = OpenAIClient(OpenAI(openAIConfig))
    val embeddings = OpenAIEmbeddings(client)
    return AIRuntime(client, embeddings) { block ->
      client.use { openAiClient ->
        val vectorStore = LocalVectorStore(embeddings)
        val openAI = XefOpenAI(openAiClient)
        val scope =
          CoreAIScope(
            defaultModel = openAI.GPT_3_5_TURBO_16K,
            defaultSerializationModel = openAI.GPT_3_5_TURBO_FUNCTIONS,
            defaultImageModel = openAI.DALLE_2,
            context = vectorStore,
            embeddings = embeddings
          )
        block(scope)
      }
    }
  }
}
