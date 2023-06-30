package com.xebia.functional.xef.auto.llm.openai

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.LLMModel

class OpenAI(client: OpenAIClient) {
  val GPT_4 = LLMModel.Chat(client, "gpt-4", ModelType.GPT_4)

  val GPT_4_0314 = LLMModel.Chat(client, "gpt-4-0314", ModelType.GPT_4)

  val GPT_4_32K = LLMModel.Chat(client, "gpt-4-32k", ModelType.GPT_4_32K)

  val GPT_3_5_TURBO = LLMModel.Chat(client, "gpt-3.5-turbo", ModelType.GPT_3_5_TURBO)

  val GPT_3_5_TURBO_16K = LLMModel.Chat(client, "gpt-3.5-turbo-16k", ModelType.GPT_3_5_TURBO_16_K)

  val GPT_3_5_TURBO_FUNCTIONS =
    LLMModel.ChatWithFunctions(client, "gpt-3.5-turbo-0613", ModelType.GPT_3_5_TURBO_FUNCTIONS)

  val GPT_3_5_TURBO_0301 = LLMModel.Chat(client, "gpt-3.5-turbo-0301", ModelType.GPT_3_5_TURBO)

  val TEXT_DAVINCI_003 = LLMModel.Completion(client, "text-davinci-003", ModelType.TEXT_DAVINCI_003)

  val TEXT_DAVINCI_002 = LLMModel.Completion(client, "text-davinci-002", ModelType.TEXT_DAVINCI_002)

  val TEXT_CURIE_001 = LLMModel.Completion(client, "text-curie-001", ModelType.TEXT_SIMILARITY_CURIE_001)

  val TEXT_BABBAGE_001 = LLMModel.Completion(client, "text-babbage-001", ModelType.TEXT_BABBAGE_001)

  val TEXT_ADA_001 = LLMModel.Completion(client, "text-ada-001", ModelType.TEXT_ADA_001)

  val DALLE_2 =
    LLMModel.Images(client, "dalle-2")
}
