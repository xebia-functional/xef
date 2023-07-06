package com.xebia.functional.xef.auto.llm.openai

import arrow.core.nonEmptyListOf
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.env.getenv
import kotlin.jvm.JvmField

class OpenAI(internal val token: String) {
  val GPT_4 = OpenAIModel(this, "gpt-4", ModelType.GPT_4)

  val GPT_4_0314 = OpenAIModel(this, "gpt-4-0314", ModelType.GPT_4)

  val GPT_4_32K = OpenAIModel(this, "gpt-4-32k", ModelType.GPT_4_32K)

  val GPT_3_5_TURBO = OpenAIModel(this, "gpt-3.5-turbo", ModelType.GPT_3_5_TURBO)

  val GPT_3_5_TURBO_16K = OpenAIModel(this, "gpt-3.5-turbo-16k", ModelType.GPT_3_5_TURBO_16_K)

  val GPT_3_5_TURBO_FUNCTIONS =
    OpenAIModel(this, "gpt-3.5-turbo-0613", ModelType.GPT_3_5_TURBO_FUNCTIONS)

  val GPT_3_5_TURBO_0301 = OpenAIModel(this, "gpt-3.5-turbo-0301", ModelType.GPT_3_5_TURBO)

  val TEXT_DAVINCI_003 = OpenAIModel(this, "text-davinci-003", ModelType.TEXT_DAVINCI_003)

  val TEXT_DAVINCI_002 = OpenAIModel(this, "text-davinci-002", ModelType.TEXT_DAVINCI_002)

  val TEXT_CURIE_001 = OpenAIModel(this, "text-curie-001", ModelType.TEXT_SIMILARITY_CURIE_001)

  val TEXT_BABBAGE_001 = OpenAIModel(this, "text-babbage-001", ModelType.TEXT_BABBAGE_001)

  val TEXT_ADA_001 = OpenAIModel(this, "text-ada-001", ModelType.TEXT_ADA_001)

  val TEXT_EMBEDDING_ADA_002 =
    OpenAIModel(this, "text-embedding-ada-002", ModelType.TEXT_EMBEDDING_ADA_002)

  val DALLE_2 = OpenAIModel(this, "dalle-2", ModelType.GPT_3_5_TURBO)

  companion object {

    fun openAITokenFromEnv(): String {
      return getenv("OPENAI_TOKEN")
        ?: throw AIError.Env.OpenAI(nonEmptyListOf("missing OPENAI_TOKEN env var"))
    }

    @JvmField val DEFAULT = OpenAI(openAITokenFromEnv())

    @JvmField val DEFAULT_CHAT = DEFAULT.GPT_3_5_TURBO_16K

    @JvmField val DEFAULT_SERIALIZATION = DEFAULT.GPT_3_5_TURBO_FUNCTIONS

    @JvmField val DEFAULT_EMBEDDING = DEFAULT.TEXT_EMBEDDING_ADA_002

    @JvmField val DEFAULT_IMAGES = DEFAULT.DALLE_2
  }
}
