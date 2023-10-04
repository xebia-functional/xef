package com.xebia.functional.xef.store

import com.xebia.functional.xef.llm.IChat
import com.xebia.functional.xef.llm.LLM

suspend fun List<Memory>.reduceByLimitToken(llm: IChat, limitTokens: Int): List<Memory> {
  val tokensFromMessages = llm.estimateTokens(map { it.content })
  return if (tokensFromMessages <= limitTokens) this
  else
    fold(Pair(0, emptyList<Memory>())) { (accTokens, list), memory ->
        val tokensFromMessage = llm.estimateTokens(listOf(memory.content))
        val totalTokens = accTokens + tokensFromMessage
        if (totalTokens <= limitTokens) {
          Pair(totalTokens, list + memory)
        } else {
          Pair(accTokens, list)
        }
      }
      .second
}
