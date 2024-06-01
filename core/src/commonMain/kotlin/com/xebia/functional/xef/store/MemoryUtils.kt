package com.xebia.functional.xef.store

import ai.xef.Chat

fun List<Memory>.reduceByLimitToken(model: Chat, limitTokens: Int): List<Memory> {
  val tokensFromMessages = model.tokenizer.tokensFromMessages(map { it.content.asRequestMessage() })
  return if (tokensFromMessages <= limitTokens) this
  else
    fold(Pair(0, emptyList<Memory>())) { (accTokens, list), memory ->
        val tokensFromMessage =
          model.tokenizer.tokensFromMessages(listOf(memory.content.asRequestMessage()))
        val totalTokens = accTokens + tokensFromMessage
        if (totalTokens + model.tokenPadding + model.tokenPaddingSum <= limitTokens) {
          Pair(totalTokens, list + memory)
        } else {
          Pair(accTokens, list)
        }
      }
      .second
}
