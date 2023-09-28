package com.xebia.functional.xef.store

fun List<Memory>.reduceByLimitToken(limitTokens: Int): List<Memory> =
  fold(Pair(0, emptyList<Memory>())) { (accTokens, list), memory ->
      val totalTokens = accTokens + memory.getApproxTokens()
      if (totalTokens <= limitTokens) {
        Pair(totalTokens, list + memory)
      } else {
        Pair(accTokens, list)
      }
    }
    .second
