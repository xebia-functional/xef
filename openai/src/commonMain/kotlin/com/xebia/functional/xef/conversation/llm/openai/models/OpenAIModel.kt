package com.xebia.functional.xef.conversation.llm.openai.models

import com.xebia.functional.tokenizer.EncodingType

interface OpenAIModel {
  val encodingType: EncodingType
  val encoding
    get() = encodingType.encoding
}
