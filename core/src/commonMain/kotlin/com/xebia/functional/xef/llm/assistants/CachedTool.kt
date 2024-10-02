package com.xebia.functional.xef.llm.assistants

import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.timeInMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

data class CachedToolKey<K>(val value: K, val seed: String)

data class CachedToolValue<V>(val value: V, val accessTimestamp: Long, val writeTimestamp: Long) {
  fun withAccessTimestamp() = copy(accessTimestamp = timeInMillis())

  companion object {
    fun <V> withActualResponse(response: V): CachedToolValue<V> =
      CachedToolValue(
        value = response,
        accessTimestamp = timeInMillis(),
        writeTimestamp = timeInMillis()
      )
  }
}

data class CachedToolConfig(
  val timeCachePolicy: Duration,
  val cacheExpirationPolicy: CacheExpirationPolicy,
  val cacheEvictionPolicy: CacheEvictionPolicy
) {

  /** Policy to expire the entries in the cache, based on last access or last write time. */
  enum class CacheExpirationPolicy {
    /** Last access time is used to determine expiration */
    ACCESS,
    /** Last write time is used to determine expiration */
    WRITE
  }

  /** Policy to evict the expired entries from the cache, based on one or all expired entries. */
  enum class CacheEvictionPolicy {
    /** Removes the expired entry when found */
    SINGLE,
    /** Removes all expired entries when one expired entry found */
    ALL
  }

  companion object {
    val Default =
      CachedToolConfig(
        timeCachePolicy = 1.days,
        cacheEvictionPolicy = CacheEvictionPolicy.ALL,
        cacheExpirationPolicy = CacheExpirationPolicy.WRITE
      )
  }
}

/**
 * Tool that caches the result of the execution of [onCacheMissed] if [shouldUseCache] returns true.
 * Otherwise, returns the result of [onCacheMissed]. This output is added to the cache when
 * [shouldCacheOutput] returns true.
 *
 * Cache is stored in a [Map] of [CachedToolKey] to [CachedToolValue].
 *
 * Supports expiration policies using [CachedToolConfig].
 */
abstract class CachedTool<Input, Output>(
  private val cache: Atomic<MutableMap<CachedToolKey<Input>, CachedToolValue<Output>>>,
  private val seed: String,
  private val config: CachedToolConfig = CachedToolConfig.Default
) : Tool<Input, Output> {
  /**
   * Logic to be executed when the cache is missed.
   *
   * @return the output.
   */
  abstract suspend fun onCacheMissed(input: Input): Output

  /**
   * Criteria to check if the cache should be used for the given [input]. By default, it returns
   * true, meaning always use the cache if available.
   *
   * @return true if the cache should be used.
   */
  open suspend fun shouldUseCache(input: Input): Boolean = true

  /**
   * Criteria to check if the result should be cached based on the given [input] and [output]. By
   * default, it returns true, meaning always cache the result.
   *
   * @return true if the result should be cached.
   */
  open suspend fun shouldCacheOutput(input: Input, output: Output): Boolean = true

  /**
   * Caches the result of [onCacheMissed] if [shouldCacheOutput] returns true. Otherwise, returns
   * the result of [onCacheMissed].
   *
   * @return the output.
   */
  override suspend fun invoke(input: Input): Output =
    if (shouldUseCache(input)) cache(CachedToolKey(input, seed)) { onCacheMissed(input) }
    else onCacheMissed(input)

  /**
   * Returns a snapshot of the cache as a [Map] of [Input] to [Output] filtered by instance [seed]
   * and removing expired cache entries with the given [config] policies. Does not modify the cache.
   *
   * @return the map of input to output.
   */
  suspend fun getValidCacheSnapshot(): Map<Input, Output> {
    val validEntries =
      cache.modify { cachedToolInfo ->
        val validEntries = cachedToolInfo.filterExpired().filter { (key, _) -> key.seed == seed }
        Pair(cachedToolInfo, validEntries)
      }
    return validEntries.map { it.key.value to it.value.value }.toMap()
  }

  private suspend fun cache(input: CachedToolKey<Input>, block: suspend () -> Output): Output =
    cache.modify { cachedToolInfo ->
      cachedToolInfo[input]?.let { output ->
        if (output.isExpired()) {
          val updatedCache =
            when (config.cacheEvictionPolicy) {
              CachedToolConfig.CacheEvictionPolicy.SINGLE -> cachedToolInfo.apply { remove(input) }
              CachedToolConfig.CacheEvictionPolicy.ALL -> cachedToolInfo.filterExpired()
            }
          Pair(updatedCache, null)
        } else {
          val updatedOutput = output.withAccessTimestamp()
          Pair(cachedToolInfo, updatedOutput.value)
        }
      } ?: Pair(cachedToolInfo, null)
    }
      ?: run {
        val response = block()
        if (shouldCacheOutput(input.value, response)) {
          cache.update { cachedToolInfo ->
            cachedToolInfo[input] = CachedToolValue.withActualResponse(response)
            cachedToolInfo
          }
        }
        response
      }

  private fun MutableMap<CachedToolKey<Input>, CachedToolValue<Output>>.filterExpired() =
    this.filter { (_, value) -> !value.isExpired() }.toMutableMap()

  private fun CachedToolValue<Output>.isExpired(): Boolean =
    when (config.cacheExpirationPolicy) {
      CachedToolConfig.CacheExpirationPolicy.ACCESS -> {
        val lastTimeInCache = timeInMillis() - accessTimestamp
        lastTimeInCache > config.timeCachePolicy.inWholeMilliseconds
      }
      CachedToolConfig.CacheExpirationPolicy.WRITE -> {
        val lastTimeInCache = timeInMillis() - writeTimestamp
        lastTimeInCache > config.timeCachePolicy.inWholeMilliseconds
      }
    }
}
