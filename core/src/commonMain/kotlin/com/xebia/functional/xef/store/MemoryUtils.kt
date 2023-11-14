package com.xebia.functional.xef.store

import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.llm.tokensFromMessages

fun List<Memory>.reduceByLimitToken(model: CreateChatCompletionRequestModel, limitTokens: Int): List<Memory> {
  val tokensFromMessages = model.modelType.tokensFromMessages(map { it.content.asRequestMessage() })
  return if (tokensFromMessages <= limitTokens) this
  else
    fold(Pair(0, emptyList<Memory>())) { (accTokens, list), memory ->
        val tokensFromMessage = model.modelType.tokensFromMessages(listOf(memory.content.asRequestMessage()))
        val totalTokens = accTokens + tokensFromMessage
        if (totalTokens <= limitTokens) {
          Pair(totalTokens, list + memory)
        } else {
          Pair(accTokens, list)
        }
      }
      .second
}
