package com.xebia.functional.xef.store

import com.xebia.functional.xef.llm.LLM

fun List<Memory>.reduceByLimitToken(llm: LLM, limitTokens: Int): List<Memory> {
  val tokensFromMessages = llm.tokensFromMessages(map { it.content.asRequestMessage() })
  return if (tokensFromMessages <= limitTokens) this
  else
    fold(Pair(0, emptyList<Memory>())) { (accTokens, list), memory ->
        val tokensFromMessage = llm.tokensFromMessages(listOf(memory.content.asRequestMessage()))
        val totalTokens = accTokens + tokensFromMessage
        if (totalTokens <= limitTokens) {
          Pair(totalTokens, list + memory)
        } else {
          Pair(accTokens, list)
        }
      }
      .second
}
