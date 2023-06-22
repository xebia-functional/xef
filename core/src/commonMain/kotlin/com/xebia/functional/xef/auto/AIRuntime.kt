package com.xebia.functional.xef.auto

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.llm.LLMModel
import com.xebia.functional.xef.llm.openai.OpenAIClient
import com.xebia.functional.xef.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import kotlin.jvm.JvmStatic
import kotlin.time.ExperimentalTime

data class AIRuntime<A>(val runtime: suspend (block: AI<A>) -> A) {
  companion object {
    @OptIn(ExperimentalTime::class)
    @JvmStatic
    fun <A> openAI(token: String? = null): AIRuntime<A> = AIRuntime { block ->
      val openAIConfig =
        OpenAIConfig(
          token = token ?:
            requireNotNull(getenv("OPENAI_TOKEN")) { "OpenAI Token missing from environment." },
        )
      val openAI = OpenAI(openAIConfig)
      OpenAIClient(openAI).use { openAiClient ->
        val embeddings = OpenAIEmbeddings(openAiClient)
        val vectorStore = LocalVectorStore(embeddings)
        val scope =
          AIScope(
            defaultModel = LLMModel.GPT_3_5_TURBO_16K,
            defaultSerializationModel = LLMModel.GPT_3_5_TURBO_FUNCTIONS,
            AIClient = openAiClient,
            context = vectorStore,
            embeddings = embeddings
          )
        block(scope)
      }
    }
  }
}
