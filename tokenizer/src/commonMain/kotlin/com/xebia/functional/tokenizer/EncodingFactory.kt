package com.xebia.functional.tokenizer

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

  private val SPECIAL_TOKENS_CL100K_BASE: Map<String, Int> = HashMap<String, Int>(5).apply {
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
    Regex("'s|'t|'re|'ve|'m|'ll|'d| ?\\p{L}+| ?\\p{N}+| ?[^\\s\\p{L}\\p{N}]+|\\s+(?!\\S)|\\s+"),
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
    Regex("'s|'t|'re|'ve|'m|'ll|'d| ?\\p{L}+| ?\\p{N}+| ?[^\\s\\p{L}\\p{N}]+|\\s+(?!\\S)|\\s+"),
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
    Regex("'s|'t|'re|'ve|'m|'ll|'d| ?\\p{L}+| ?\\p{N}+| ?[^\\s\\p{L}\\p{N}]+|\\s+(?!\\S)|\\s+", RegexOption.IGNORE_CASE),
    p50k_base,
    SPECIAL_TOKENS_P50K_EDIT
  )

  fun cl100kBase(): Encoding = fromPredefinedParameters(
    "cl100k_base",
    regex,
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

  @OptIn(ExperimentalEncodingApi::class)
  private fun loadMergeableRanks(base: String): Map<ByteArray, Int> =
    buildMap {
      base.lineSequence().forEach { line ->
        val (token, rank) = line.split(Regex("\\s+"), limit = 2)
        put(Base64.decode(token.encodeToByteArray()), rank.toInt())
      }
    }
}

expect val regex: Regex
