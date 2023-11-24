package com.xebia.functional.xef.conversation.llm.openai.models

import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.xef.llm.LLM

interface OpenAIModel : LLM {

  val encodingType: EncodingType
  val encoding
    get() = encodingType.encoding
}
