package com.xebia.functional.xef.llm.assistants

import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.timeInMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

abstract class CachedTool<Input, Output>(
  private val cache: Atomic<MutableMap<Input, CachedToolInfo<Output>>>,
  private val timeCachePolicy: Duration = 1.days
) : Tool<Input, Output> {

  override suspend fun invoke(input: Input): Output {
    return cache(input) { onCacheMissed(input) }
  }

  abstract suspend fun onCacheMissed(input: Input): Output

  private suspend fun cache(input: Input, block: suspend () -> Output): Output {
    val cachedToolInfo = cache.get().get(input)
    if (cachedToolInfo != null) {
      val lastTimeInCache = timeInMillis() - timeCachePolicy.inWholeMilliseconds
      if (lastTimeInCache > cachedToolInfo.timestamp) {
        cache.get().remove(input)
      } else {
        return cachedToolInfo.response
      }
    }
    val response = block()
    cache.get().put(input, CachedToolInfo(response, timeInMillis()))
    return response
  }
}
