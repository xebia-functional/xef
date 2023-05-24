@file:OptIn(ExperimentalEncodingApi::class)

package com.xebia.functional.tokenizer

import com.xebia.functional.tokenizer.internal.cl100k_base
import com.xebia.functional.tokenizer.internal.p50k_base
import com.xebia.functional.tokenizer.internal.r50k_base
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

enum class EncodingType(@Suppress("UNUSED_PARAMETER") name: String) {
  R50K_BASE("r50k_base") {
    override val encoding by lazy { EncodingFactory.r50kBase() }
  },
  P50K_BASE("p50k_base") {
    override val encoding by lazy { EncodingFactory.p50kBase() }
  },
  P50K_EDIT("p50k_edit") {
    override val encoding by lazy { EncodingFactory.p50kEdit() }
  },
  CL100K_BASE("cl100k_base") {
    override val encoding by lazy { EncodingFactory.cl100kBase() }
  };

  abstract val encoding: Encoding
}

object EncodingFactory {
  private const val ENDOFTEXT = "<|endoftext|>"
  private const val FIM_PREFIX = "<|fim_prefix|>"
  private const val FIM_MIDDLE = "<|fim_middle|>"
  private const val FIM_SUFFIX = "<|fim_suffix|>"
  private const val ENDOFPROMPT = "<|endofprompt|>"

  private val SPECIAL_TOKENS_X50K_BASE: Map<String, Int> = HashMap<String, Int>(1).apply {
    put(ENDOFTEXT, 50256)
  }

  private val SPECIAL_TOKENS_P50K_EDIT: Map<String, Int> = HashMap<String, Int>(4).apply {
    put(ENDOFTEXT, 50256)
    put(FIM_PREFIX, 50281)
    put(FIM_MIDDLE, 50282)
    put(FIM_SUFFIX, 50283)
  }

  val SPECIAL_TOKENS_CL100K_BASE: Map<String, Int> = HashMap<String, Int>(5).apply {
    put(ENDOFTEXT, 100257)
    put(FIM_PREFIX, 100258)
    put(FIM_MIDDLE, 100259)
    put(FIM_SUFFIX, 100260)
    put(ENDOFPROMPT, 100276)
  }

  /**
   * Returns an [Encoding] instance for the r50k_base encoding.
   *
   * @return an [Encoding] instance for the r50k_base encoding
   */
  fun r50kBase(): Encoding = fromPredefinedParameters(
    "r50k_base",
    p50k_regex,
    r50k_base,
    SPECIAL_TOKENS_X50K_BASE
  )

  /**
   * Returns an [Encoding] instance for the p50k_base encoding.
   *
   * @return an [Encoding] instance for the p50k_base encoding
   */
  fun p50kBase(): Encoding = fromPredefinedParameters(
    "p50k_base",
    p50k_regex,
    p50k_base,
    SPECIAL_TOKENS_X50K_BASE
  )

  /**
   * Returns an [Encoding] instance for the p50k_edit encoding.
   *
   * @return an [Encoding] instance for the p50k_edit encoding
   */
  fun p50kEdit(): Encoding = fromPredefinedParameters(
    "p50k_edit",
    p50k_regex,
    p50k_base,
    SPECIAL_TOKENS_P50K_EDIT
  )

  fun cl100kBase(): Encoding = fromPredefinedParameters(
    "cl100k_base",
    cl100k_base_regex,
    cl100k_base,
    SPECIAL_TOKENS_CL100K_BASE
  )

  /**
   * Returns an [Encoding] instance for the given GPT BytePairEncoding parameters.
   *
   * @param parameters the GPT BytePairEncoding parameters
   * @return an [Encoding] instance for the given GPT BytePairEncoding parameters
   */
  fun fromParameters(parameters: GptBytePairEncodingParams): Encoding =
    GptBytePairEncoding(parameters)

  private fun fromPredefinedParameters(
    name: String,
    regex: Regex,
    base: String,
    specialTokens: Map<String, Int>
  ): Encoding {
    val params = GptBytePairEncodingParams(name, regex, loadMergeableRanks(base), specialTokens)
    return fromParameters(params)
  }

  fun loadMergeableRanks(base: String): Map<ByteArray, Int> =
    buildMap {
      base.lineSequence().forEach { line ->
        val (token, rank) = line.split(Regex("\\s+"), limit = 2)
        put(Base64.decode(token.encodeToByteArray()), rank.toInt())
      }
    }
}

expect val cl100k_base_regex: Regex
expect val p50k_regex: Regex
