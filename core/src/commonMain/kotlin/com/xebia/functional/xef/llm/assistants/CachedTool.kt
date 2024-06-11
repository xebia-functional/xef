package com.xebia.functional.xef.llm.assistants

import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.timeInMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

data class CachedToolKey<K>(val value: K, val seed: String)

data class CachedToolValue<V>(val value: V, val timestamp: Long)

abstract class CachedTool<Input, Output>(
  private val cache: Atomic<MutableMap<CachedToolKey<Input>, CachedToolValue<Output>>>,
  private val seed: String,
  private val timeCachePolicy: Duration = 1.days
) : Tool<Input, Output> {

  override suspend fun invoke(input: Input): Output {
    return cache(CachedToolKey(input, seed)) { onCacheMissed(input) }
  }

  abstract suspend fun onCacheMissed(input: Input): Output

  private suspend fun cache(input: CachedToolKey<Input>, block: suspend () -> Output): Output {
    val cachedToolInfo = cache.get().get(input)
    if (cachedToolInfo != null) {
      val lastTimeInCache = timeInMillis() - timeCachePolicy.inWholeMilliseconds
      if (lastTimeInCache > cachedToolInfo.timestamp) {
        cache.get().remove(input)
      } else {
        return cachedToolInfo.value
      }
    }
    val response = block()
    cache.get().put(input, CachedToolValue(response, timeInMillis()))
    return response
  }
}
