package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.ModelType
import kotlin.jvm.JvmStatic

sealed interface LLM {
  val name: String

  interface Chat : LLM {
    val client: AIClient.Chat
    val modelType: ModelType
  }

  interface Completion : LLM {
    val client: AIClient.Completion
    val modelType: ModelType
  }

  interface ChatWithFunctions : Chat {
    override val client: AIClient.ChatWithFunctions
  }

  interface Embedding : LLM {
    val client: AIClient.Embeddings
    val modelType: ModelType
  }

  interface Images : LLM {
    val client: AIClient.Images
  }
}

sealed class LLMModel(override val name: String) : LLM {

  data class Chat(override val client: AIClient.Chat, override val name: String, override val modelType: ModelType) :
    LLMModel(name), LLM.Chat

  data class Completion(override val client: AIClient.Completion, override val name: String, override val modelType: ModelType) :
    LLMModel(name), LLM.Completion

  data class ChatWithFunctions(override val client: AIClient.ChatWithFunctions, override val name: String, override val modelType: ModelType) :
    LLMModel(name), LLM.ChatWithFunctions

  data class Embedding(override val client: AIClient.Embeddings, override val name: String, override val modelType: ModelType) :
    LLMModel(name), LLM.Embedding

  data class Images(override val client: AIClient.Images, override val name: String) : LLM.Images

}
