package com.xebia.functional.xef.llm

import com.xebia.functional.openai.models.ext.chat.create.CreateChatCompletionRequestModel
import com.xebia.functional.tokenizer.ModelType

internal fun ModelType.toRequestModel(): CreateChatCompletionRequestModel = when (name) {
  ModelType.GPT_4.name -> CreateChatCompletionRequestModel.gpt_4
  ModelType.GPT_4_32K.name -> CreateChatCompletionRequestModel.gpt_4_32k
  ModelType.GPT_4_0314.name -> CreateChatCompletionRequestModel.gpt_4_0314
  ModelType.GPT_3_5_TURBO.name -> CreateChatCompletionRequestModel.gpt_3_5_turbo
  ModelType.GPT_3_5_TURBO_16_K.name -> CreateChatCompletionRequestModel.gpt_3_5_turbo_16k
  ModelType.GPT_3_5_TURBO_FUNCTIONS.name -> CreateChatCompletionRequestModel.gpt_3_5_turbo_0613
  else -> throw IllegalArgumentException("Model ${name} is not supported")
}
