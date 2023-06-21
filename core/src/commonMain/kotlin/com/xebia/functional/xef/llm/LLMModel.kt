package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.ModelType
import kotlin.jvm.JvmStatic

sealed interface LLM {
  val name: String
  val modelType: ModelType

  interface Chat : LLM

  interface Completion : LLM

  interface ChatWithFunctions : Chat

  interface Embedding : LLM

  interface Images : LLM
}

sealed class LLMModel(override val name: String, override val modelType: ModelType) : LLM {

  data class Chat(override val name: String, override val modelType: ModelType) :
    LLMModel(name, modelType), LLM.Chat

  data class Completion(override val name: String, override val modelType: ModelType) :
    LLMModel(name, modelType), LLM.Completion

  data class ChatWithFunctions(override val name: String, override val modelType: ModelType) :
    LLMModel(name, modelType), LLM.ChatWithFunctions

  data class Embedding(override val name: String, override val modelType: ModelType) :
    LLMModel(name, modelType), LLM.Embedding

  companion object {
    @JvmStatic val GPT_4 = Chat("gpt-4", ModelType.GPT_4)

    @JvmStatic val GPT_4_0314 = Chat("gpt-4-0314", ModelType.GPT_4)

    @JvmStatic val GPT_4_32K = Chat("gpt-4-32k", ModelType.GPT_4_32K)

    @JvmStatic val GPT_3_5_TURBO = Chat("gpt-3.5-turbo", ModelType.GPT_3_5_TURBO)

    @JvmStatic val GPT_3_5_TURBO_16K = Chat("gpt-3.5-turbo-16k", ModelType.GPT_3_5_TURBO_16_K)

    @JvmStatic
    val GPT_3_5_TURBO_FUNCTIONS =
      ChatWithFunctions("gpt-3.5-turbo-0613", ModelType.GPT_3_5_TURBO_FUNCTIONS)

    @JvmStatic val GPT_3_5_TURBO_0301 = Chat("gpt-3.5-turbo-0301", ModelType.GPT_3_5_TURBO)

    @JvmStatic val TEXT_DAVINCI_003 = Completion("text-davinci-003", ModelType.TEXT_DAVINCI_003)

    @JvmStatic val TEXT_DAVINCI_002 = Completion("text-davinci-002", ModelType.TEXT_DAVINCI_002)

    @JvmStatic
    val TEXT_CURIE_001 = Completion("text-curie-001", ModelType.TEXT_SIMILARITY_CURIE_001)

    @JvmStatic val TEXT_BABBAGE_001 = Completion("text-babbage-001", ModelType.TEXT_BABBAGE_001)

    @JvmStatic val TEXT_ADA_001 = Completion("text-ada-001", ModelType.TEXT_ADA_001)
  }
}
