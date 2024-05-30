@file:OptIn(ExperimentalEncodingApi::class)

package com.xebia.functional.tokenizer

import com.xebia.functional.tokenizer.internal.SPECIAL_TOKENS_CL100K_BASE
import com.xebia.functional.tokenizer.internal.SPECIAL_TOKENS_O200K_BASE
import com.xebia.functional.tokenizer.internal.SPECIAL_TOKENS_P50K_EDIT
import com.xebia.functional.tokenizer.internal.SPECIAL_TOKENS_X50K_BASE
import com.xebia.functional.tokenizer.internal.cl100k_base
import com.xebia.functional.tokenizer.internal.o200k_base
import com.xebia.functional.tokenizer.internal.p50k_base
import com.xebia.functional.tokenizer.internal.r50k_base
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

enum class EncodingType(@Suppress("UNUSED_PARAMETER") name: String) {
    R50K_BASE("r50k_base") {
        override val base: String = r50k_base
        override val regex: Regex = p50k_regex
        override val specialTokensBase: Map<String, Int> = SPECIAL_TOKENS_P50K_EDIT
        override val encoding by lazy {
            EncodingFactory.fromPredefinedParameters(
                name, regex, base, specialTokensBase
            )
        }
    },
    P50K_BASE("p50k_base") {
      override val base: String = p50k_base
      override val regex: Regex = p50k_regex
      override val specialTokensBase: Map<String, Int> = SPECIAL_TOKENS_X50K_BASE
      override val encoding by lazy {
        EncodingFactory.fromPredefinedParameters(
          name, regex, base, specialTokensBase
        )
      }
    },
    P50K_EDIT("p50k_edit") {
      override val base: String = p50k_base
      override val regex: Regex = p50k_regex
      override val specialTokensBase: Map<String, Int> = SPECIAL_TOKENS_P50K_EDIT
      override val encoding by lazy {
        EncodingFactory.fromPredefinedParameters(
          name, regex, base, specialTokensBase
        )
      }
    },
    CL100K_BASE("cl100k_base") {
        override val base: String = cl100k_base
        override val regex: Regex = cl100k_base_regex
        override val specialTokensBase: Map<String, Int> = SPECIAL_TOKENS_CL100K_BASE
        override val encoding by lazy {
            EncodingFactory.fromPredefinedParameters(
                name, regex, base, specialTokensBase
            )
        }
    },
    O200K_BASE("o200k_base") {
      override val base: String = o200k_base
      override val regex: Regex = o200k_base_regex
      override val specialTokensBase: Map<String, Int> = SPECIAL_TOKENS_O200K_BASE
      override val encoding by lazy {
        EncodingFactory.fromPredefinedParameters(
          name, regex, base, specialTokensBase
        )
      }
    };

    abstract val base: String
    abstract val regex: Regex
    abstract val specialTokensBase: Map<String, Int>
    abstract val encoding: Encoding
}

private object EncodingFactory {
    fun fromPredefinedParameters(
        name: String,
        regex: Regex,
        base: String,
        specialTokens: Map<String, Int>
    ): Encoding {
        val params = GptBytePairEncodingParams(name, regex, loadMergeableRanks(base), specialTokens)
        return fromParameters(params)
    }

    private fun fromParameters(parameters: GptBytePairEncodingParams): Encoding =
        GptBytePairEncoding(parameters)

    @OptIn(ExperimentalEncodingApi::class)
    fun loadMergeableRanks(base: String): Map<ByteArray, Int> =
        buildMap {
            base.lineSequence().forEach { line ->
                val (token, rank) = line.split(Regex("\\s+"), limit = 2)
                put(Base64.decode(token.encodeToByteArray()), rank.toInt())
            }
        }
}

expect val p50k_regex: Regex
expect val cl100k_base_regex: Regex
expect val o200k_base_regex: Regex
