package com.xebia.functional.xef.vectorstores

fun List<Memory>.reduceByLimitToken(limitTokens: Int): List<Memory> =
  fold(Pair(0, emptyList<Memory>())) { (accTokens, list), memory ->
      val totalTokens = accTokens + memory.approxTokens
      if (totalTokens <= limitTokens) {
        Pair(totalTokens, list + memory)
      } else {
        Pair(accTokens, list)
      }
    }
    .second
