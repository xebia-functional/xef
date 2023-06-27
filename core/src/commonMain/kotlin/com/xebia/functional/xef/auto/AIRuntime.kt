package com.xebia.functional.xef.auto

import com.xebia.functional.xef.embeddings.OpenAIEmbeddings
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.llm.openai.KtorOpenAIClient
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import kotlin.jvm.JvmStatic
import kotlin.time.ExperimentalTime

data class AIRuntime<A>(val runtime: suspend (block: AI<A>) -> A) {
  companion object {
    @OptIn(ExperimentalTime::class)
    @JvmStatic
    fun <A> openAI(): AIRuntime<A> = AIRuntime { block ->
      val openAIConfig = OpenAIConfig()
      KtorOpenAIClient(openAIConfig).use { openAiClient ->
        val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient)
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
