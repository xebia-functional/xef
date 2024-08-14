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

  /**
   * Exposes the cache as a [Map] of [Input] to [Output] filtered by instance [seed] and
   * [timeCachePolicy]. Removes expired cache entries.
   */
  suspend fun getCache(): Map<Input, Output> {
    val lastTimeInCache = timeInMillis() - timeCachePolicy.inWholeMilliseconds
    val withoutExpired =
      cache.modify { cachedToolInfo ->
        // Filter entries belonging to the current seed and have not expired
        val validEntries =
          cachedToolInfo
            .filter { (key, value) ->
              if (key.seed == seed) lastTimeInCache <= value.timestamp else true
            }
            .toMutableMap()
        // Remove expired entries for the current seed only
        cachedToolInfo.keys.removeAll { key -> key.seed == seed && !validEntries.containsKey(key) }
        // Modifies state A, and returns state B
        Pair(cachedToolInfo, validEntries)
      }
    return withoutExpired.map { it.key.value to it.value.value }.toMap()
  }

  abstract suspend fun onCacheMissed(input: Input): Output

  private suspend fun cache(input: CachedToolKey<Input>, block: suspend () -> Output): Output {
    val cachedToolInfo = cache.get()[input]
    if (cachedToolInfo != null) {
      val lastTimeInCache = timeInMillis() - timeCachePolicy.inWholeMilliseconds
      if (lastTimeInCache > cachedToolInfo.timestamp) {
        cache.get().remove(input)
      } else {
        return cachedToolInfo.value
      }
    }
    val response = block()
    cache.get()[input] = CachedToolValue(response, timeInMillis())
    return response
  }
}
