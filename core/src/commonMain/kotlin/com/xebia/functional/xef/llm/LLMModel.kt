package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.ModelType
import kotlin.jvm.JvmStatic

data class LLMModel(val name: String, val kind: Kind, val modelType: ModelType) {

  enum class Kind {
    Completion,
    Chat,
    ChatWithFunctions,
  }

  companion object {
    @JvmStatic val GPT_4 = LLMModel("gpt-4", Kind.Chat, ModelType.GPT_4)

    @JvmStatic val GPT_4_0314 = LLMModel("gpt-4-0314", Kind.Chat, ModelType.GPT_4)

    @JvmStatic val GPT_4_32K = LLMModel("gpt-4-32k", Kind.Chat, ModelType.GPT_4_32K)

    @JvmStatic val GPT_3_5_TURBO = LLMModel("gpt-3.5-turbo", Kind.Chat, ModelType.GPT_3_5_TURBO)

    @JvmStatic
    val GPT_3_5_TURBO_16K = LLMModel("gpt-3.5-turbo-16k", Kind.Chat, ModelType.GPT_3_5_TURBO_16_K)

    @JvmStatic
    val GPT_3_5_TURBO_FUNCTIONS =
      LLMModel("gpt-3.5-turbo-0613", Kind.ChatWithFunctions, ModelType.GPT_3_5_TURBO_FUNCTIONS)

    @JvmStatic
    val GPT_3_5_TURBO_0301 = LLMModel("gpt-3.5-turbo-0301", Kind.Chat, ModelType.GPT_3_5_TURBO)

    @JvmStatic
    val TEXT_DAVINCI_003 = LLMModel("text-davinci-003", Kind.Completion, ModelType.TEXT_DAVINCI_003)

    @JvmStatic
    val TEXT_DAVINCI_002 = LLMModel("text-davinci-002", Kind.Completion, ModelType.TEXT_DAVINCI_002)

    @JvmStatic
    val TEXT_CURIE_001 =
      LLMModel("text-curie-001", Kind.Completion, ModelType.TEXT_SIMILARITY_CURIE_001)

    @JvmStatic
    val TEXT_BABBAGE_001 = LLMModel("text-babbage-001", Kind.Completion, ModelType.TEXT_BABBAGE_001)

    @JvmStatic val TEXT_ADA_001 = LLMModel("text-ada-001", Kind.Completion, ModelType.TEXT_ADA_001)
  }
}
