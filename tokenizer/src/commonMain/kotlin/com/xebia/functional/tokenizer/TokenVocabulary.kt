package com.xebia.functional.tokenizer

interface TokenVocabulary {
    val decodedTokens: Map<Int, String>

    companion object {
        operator fun invoke(encodingType: EncodingType): TokenVocabulary =
            object : TokenVocabulary {
                override val decodedTokens: Map<Int, String> = encodingType.buildDecodedTokenVocabulary()

                private fun EncodingType.buildDecodedTokenVocabulary(): Map<Int, String> = buildMap {
                    base.lineSequence().forEach { line ->
                        val (_, rank) = line.split(Regex("\\s+"), limit = 2)
                        val tokenId: Int = rank.toInt()
                        val token: String = encodingType.encoding.decode(listOf(tokenId))
                        put(tokenId, token)
                    }
                    specialTokensBase.forEach { put(it.value, it.key) }
                }
            }
    }
}
