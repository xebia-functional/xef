package com.xebia.functional.xef.llm

import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.chat.Message

interface BaseChat : LLM {

  val contextLength: MaxIoContextLength

  @Deprecated(
    "will be removed from LLM in favor of abstracting former ModelType, use contextLength instead"
  )
  val maxContextLength
    get() =
      (contextLength as? MaxIoContextLength.Combined)?.total
        ?: error(
          "accessing maxContextLength requires model's context length to be of type MaxIoContextLength.Combined"
        )

  @Suppress("OVERRIDE_DEPRECATION") fun countTokens(text: String): Int

  @Suppress("OVERRIDE_DEPRECATION") fun truncateText(text: String, maxTokens: Int): String

  @Suppress("OVERRIDE_DEPRECATION") fun tokensFromMessages(messages: List<Message>): Int
}
