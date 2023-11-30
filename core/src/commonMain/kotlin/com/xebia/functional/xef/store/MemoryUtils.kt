package com.xebia.functional.xef.store

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.tokensFromMessages

fun List<Memory>.reduceByLimitToken(modelType: ModelType, limitTokens: Int): List<Memory> {
  val tokensFromMessages = modelType.tokensFromMessages(map { it.content.asRequestMessage() })
  return if (tokensFromMessages <= limitTokens) this
  else
    fold(Pair(0, emptyList<Memory>())) { (accTokens, list), memory ->
        val tokensFromMessage =
          modelType.tokensFromMessages(listOf(memory.content.asRequestMessage()), false)
        val totalTokens = accTokens + tokensFromMessage
        if (totalTokens + modelType.tokenPadding + modelType.tokenPaddingSum <= limitTokens) {
          Pair(totalTokens, list + memory)
        } else {
          Pair(accTokens, list)
        }
      }
      .second
}
