package com.xebia.functional.xef.conversation.llm.openai.models

import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.llm.models.chat.Message

interface OpenAIModel : LLM {

  val encodingType: EncodingType
  val encoding
    get() = encodingType.encoding

}
