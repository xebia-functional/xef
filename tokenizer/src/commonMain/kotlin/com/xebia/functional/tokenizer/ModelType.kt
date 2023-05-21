package com.xebia.functional.tokenizer

import com.xebia.functional.tokenizer.EncodingType.CL100K_BASE
import com.xebia.functional.tokenizer.EncodingType.P50K_BASE
import com.xebia.functional.tokenizer.EncodingType.R50K_BASE

enum class ModelType(
  /**
   * Returns the name of the model type as used by the OpenAI API.
   *
   * @return the name of the model type
   */
  name: String,
  val encodingType: EncodingType,
  /**
   * Returns the maximum context length that is supported by this model type. Note that
   * the maximum context length consists of the amount of prompt tokens and the amount of
   * completion tokens (where applicable).
   *
   * @return the maximum context length for this model type
   */
  val maxContextLength: Int
) {
  // chat
  GPT_4("gpt-4", CL100K_BASE, 8192),
  GPT_4_32K("gpt-4-32k", CL100K_BASE, 32768),
  GPT_3_5_TURBO("gpt-3.5-turbo", CL100K_BASE, 4097),

  // text
  TEXT_DAVINCI_003("text-davinci-003", P50K_BASE, 4097),
  TEXT_DAVINCI_002("text-davinci-002", P50K_BASE, 4097),
  TEXT_DAVINCI_001("text-davinci-001", R50K_BASE, 2049),
  TEXT_CURIE_001("text-curie-001", R50K_BASE, 2049),
  TEXT_BABBAGE_001("text-babbage-001", R50K_BASE, 2049),
  TEXT_ADA_001("text-ada-001", R50K_BASE, 2049),
  DAVINCI("davinci", R50K_BASE, 2049),
  CURIE("curie", R50K_BASE, 2049),
  BABBAGE("babbage", R50K_BASE, 2049),
  ADA("ada", R50K_BASE, 2049),

  // code
  CODE_DAVINCI_002("code-davinci-002", P50K_BASE, 8001),
  CODE_DAVINCI_001("code-davinci-001", P50K_BASE, 8001),
  CODE_CUSHMAN_002("code-cushman-002", P50K_BASE, 2048),
  CODE_CUSHMAN_001("code-cushman-001", P50K_BASE, 2048),
  DAVINCI_CODEX("davinci-codex", P50K_BASE, 4096),
  CUSHMAN_CODEX("cushman-codex", P50K_BASE, 2048),

  // edit
  TEXT_DAVINCI_EDIT_001("text-davinci-edit-001", EncodingType.P50K_EDIT, 3000),
  CODE_DAVINCI_EDIT_001("code-davinci-edit-001", EncodingType.P50K_EDIT, 3000),

  // embeddings
  TEXT_EMBEDDING_ADA_002("text-embedding-ada-002", CL100K_BASE, 8191),

  // old embeddings
  TEXT_SIMILARITY_DAVINCI_001("text-similarity-davinci-001", R50K_BASE, 2046),
  TEXT_SIMILARITY_CURIE_001("text-similarity-curie-001", R50K_BASE, 2046),
  TEXT_SIMILARITY_BABBAGE_001("text-similarity-babbage-001", R50K_BASE, 2046),
  TEXT_SIMILARITY_ADA_001("text-similarity-ada-001", R50K_BASE, 2046),
  TEXT_SEARCH_DAVINCI_DOC_001("text-search-davinci-doc-001", R50K_BASE, 2046),
  TEXT_SEARCH_CURIE_DOC_001("text-search-curie-doc-001", R50K_BASE, 2046),
  TEXT_SEARCH_BABBAGE_DOC_001("text-search-babbage-doc-001", R50K_BASE, 2046),
  TEXT_SEARCH_ADA_DOC_001("text-search-ada-doc-001", R50K_BASE, 2046),
  CODE_SEARCH_BABBAGE_CODE_001("code-search-babbage-code-001", R50K_BASE, 2046),
  CODE_SEARCH_ADA_CODE_001("code-search-ada-code-001", R50K_BASE, 2046);

  inline val encoding: Encoding
    inline get() = encodingType.encoding

  companion object {
    fun fromName(name: String): ModelType? =
      values().firstOrNull { it.name == name }
  }
}

/**
 * Truncates the given [text] to the given [maxTokens] by removing tokens
 * from the end of the text.
 * It removes tokens from the tail of the [text].
 * Tokens are chosen to be removed based on the percentage of the [maxTokens]
 * compared to the total amount of tokens in the [text].
 */
fun Encoding.truncateText(text: String, maxTokens: Int): String {
  val tokenCount = countTokens(text)
  return if (tokenCount <= maxTokens) {
    text
  } else {
    val percentage = maxTokens.toDouble() / tokenCount.toDouble()
    val truncatedTextLength = (text.length * percentage).toInt()
    text.substring(0, truncatedTextLength)
  }
}
