package com.xebia.functional.tokenizer

import com.xebia.functional.tokenizer.EncodingType.CL100K_BASE
import com.xebia.functional.tokenizer.EncodingType.P50K_BASE
import com.xebia.functional.tokenizer.EncodingType.R50K_BASE
import kotlin.jvm.JvmStatic

/**
 * Formal description of a model and it's properties
 * without any capabilities.
 */
sealed class ModelType(
  /**
   * Returns the name of the model type as used by the OpenAI API.
   *
   * @return the name of the model type
   */
  open val name: String,
  open val encodingType: EncodingType,
  /**
   * Returns the maximum context length that is supported by this model type. Note that
   * the maximum context length consists of the amount of prompt tokens and the amount of
   * completion tokens (where applicable).
   *
   * @return the maximum context length for this model type
   */
  open val maxContextLength: Int,

  open val tokensPerMessage: Int = 0,
  open val tokensPerName: Int = 0,
  open val tokenPadding: Int = 20,
  open val tokenPaddingSum: Int = 3,
) {

  companion object {
    @JvmStatic
    val DEFAULT_SPLITTER_MODEL = GPT_3_5_TURBO
    val functionSpecific: List<ModelType> = listOf(
      GPT_3_5_TURBO_0613,
      GPT_3_5_16K_0613_TURBO_FUNCTIONS,
      GPT_4_32K_0314_FUNCTIONS,
      GPT_4_0613,
      GPT_4_32K_0613_FUNCTIONS,
    )
    val all: List<ModelType> = listOf(
      GPT_3_5_TURBO,
      GPT_3_5_TURBO_0125,
      GPT_3_5_TURBO_0301,
      GPT_3_5_TURBO_0613,
      GPT_3_5_TURBO_FUNCTIONS,
      GPT_3_5_TURBO_16_K,
      GPT_3_5_TURBO_16_K_1106,
      GPT_4,
      GPT_4_0314,
      GPT_4_0613,
      GPT_4_32K,
      GPT_4_32_K_0314,
      GPT_4_32K_0613_FUNCTIONS,
      GPT_4_TURBO_1106_PREVIEW,
      GPT_4_VISION_PREVIEW,
      TEXT_DAVINCI_003,
      TEXT_DAVINCI_002,
      TEXT_DAVINCI_001,
      TEXT_CURIE_001,
      TEXT_BABBAGE_001,
      TEXT_ADA_001,
      DAVINCI,
      CURIE,
      BABBAGE,
      ADA,
      CODE_DAVINCI_002,
      CODE_DAVINCI_001,
      CODE_CUSHMAN_002,
      CODE_CUSHMAN_001,
      DAVINCI_CODEX,
      CUSHMAN_CODEX,
      TEXT_DAVINCI_EDIT_001,
      CODE_DAVINCI_EDIT_001,
      TEXT_EMBEDDING_ADA_002,
      TEXT_SIMILARITY_DAVINCI_001,
      TEXT_SIMILARITY_CURIE_001,
      TEXT_SIMILARITY_BABBAGE_001,
      TEXT_SIMILARITY_ADA_001,
      TEXT_SEARCH_DAVINCI_DOC_001,
      TEXT_SEARCH_CURIE_DOC_001,
      TEXT_SEARCH_BABBAGE_DOC_001,
      TEXT_SEARCH_ADA_DOC_001,
      CODE_SEARCH_BABBAGE_CODE_001,
      CODE_SEARCH_ADA_CODE_001,
    )
  }

  data class LocalModel(
    override val name: String,
    override val encodingType: EncodingType,
    override val maxContextLength: Int
  ) : ModelType(name, encodingType, maxContextLength)

  object GPT_3_5_TURBO :
    ModelType("gpt-3.5-turbo", CL100K_BASE, 16385, tokensPerMessage = 4, tokensPerName = 0, tokenPadding = 5)

  object GPT_3_5_TURBO_0125 :
    ModelType("gpt-3.5-turbo_0125", CL100K_BASE, 16385, tokensPerMessage = 4, tokensPerName = 0, tokenPadding = 5)

  object GPT_3_5_TURBO_0301 :
    ModelType("gpt-3.5-turbo-0301", CL100K_BASE, 4097, tokensPerMessage = 4, tokensPerName = 0, tokenPadding = 5)

  object GPT_3_5_TURBO_0613 :
    ModelType("gpt-3.5-turbo-0613", CL100K_BASE, 4097, tokensPerMessage = 3, tokensPerName = 2, tokenPadding = 5)

  object GPT_3_5_TURBO_FUNCTIONS :
    ModelType("gpt-3.5-turbo-0613", CL100K_BASE, 4097, tokensPerMessage = 4, tokensPerName = 2, tokenPadding = 5)

  object GPT_3_5_TURBO_16_K :
    ModelType("gpt-3.5-turbo-16k", CL100K_BASE, 16385, tokensPerMessage = 4, tokensPerName = 0, tokenPadding = 5)

  object GPT_3_5_TURBO_16_K_1106 :
    ModelType("gpt-3.5-turbo-1106", CL100K_BASE, 16385, tokensPerMessage = 4, tokensPerName = 0, tokenPadding = 5)

  object GPT_3_5_16K_0613_TURBO_FUNCTIONS :
    ModelType("gpt-3.5-turbo-1106", CL100K_BASE, 16385, tokensPerMessage = 4, tokensPerName = 2, tokenPadding = 5)

  object GPT_4 : ModelType("gpt-4", CL100K_BASE, 8192, tokensPerMessage = 3, tokensPerName = 2, tokenPadding = 5)
  object GPT_4_0314 :
    ModelType("gpt-4-0314", CL100K_BASE, 8192, tokensPerMessage = 3, tokensPerName = 2, tokenPadding = 5)

  object GPT_4_0613 :
    ModelType("gpt-4-0613", CL100K_BASE, 8192, tokensPerMessage = 3, tokensPerName = 2, tokenPadding = 5)

  object GPT_4_32K :
    ModelType("gpt-4-32k", CL100K_BASE, 32768, tokensPerMessage = 3, tokensPerName = 2, tokenPadding = 5)

  object GPT_4_32K_0314_FUNCTIONS :
    ModelType("gpt-4-32k-0314", CL100K_BASE, 32768, tokensPerMessage = 3, tokensPerName = 2, tokenPadding = 5)

  object GPT_4_32_K_0314 :
    ModelType("gpt-4-32k-0314", CL100K_BASE, 32768, tokensPerMessage = 3, tokensPerName = 2, tokenPadding = 5)

  object GPT_4_32K_0613_FUNCTIONS :
    ModelType("gpt-4-32k-0613", CL100K_BASE, 32768, tokensPerMessage = 3, tokensPerName = 2, tokenPadding = 5)

  object GPT_4_TURBO_1106_PREVIEW :
    ModelType("gpt-4-1106-preview", CL100K_BASE, 128000, tokensPerMessage = 3, tokensPerName = 2, tokenPadding = 5)

  object GPT_4_VISION_PREVIEW :
    ModelType("gpt-4-vision-preview", CL100K_BASE, 128000, tokensPerMessage = 3, tokensPerName = 2, tokenPadding = 5)

  // text
  object TEXT_DAVINCI_003 : ModelType("text-davinci-003", P50K_BASE, 4097)
  object TEXT_DAVINCI_002 : ModelType("text-davinci-002", P50K_BASE, 4097)
  object TEXT_DAVINCI_001 : ModelType("text-davinci-001", R50K_BASE, 2049)
  object TEXT_CURIE_001 : ModelType("text-curie-001", R50K_BASE, 2049)
  object TEXT_BABBAGE_001 : ModelType("text-babbage-001", R50K_BASE, 2049)
  object TEXT_ADA_001 : ModelType("text-ada-001", R50K_BASE, 2049)
  object DAVINCI : ModelType("davinci", R50K_BASE, 2049)
  object CURIE : ModelType("curie", R50K_BASE, 2049)
  object BABBAGE : ModelType("babbage", R50K_BASE, 2049)
  object ADA : ModelType("ada", R50K_BASE, 2049)

  // code
  object CODE_DAVINCI_002 : ModelType("code-davinci-002", P50K_BASE, 8001)
  object CODE_DAVINCI_001 : ModelType("code-davinci-001", P50K_BASE, 8001)
  object CODE_CUSHMAN_002 : ModelType("code-cushman-002", P50K_BASE, 2048)
  object CODE_CUSHMAN_001 : ModelType("code-cushman-001", P50K_BASE, 2048)
  object DAVINCI_CODEX : ModelType("davinci-codex", P50K_BASE, 4096)
  object CUSHMAN_CODEX : ModelType("cushman-codex", P50K_BASE, 2048)

  // edit
  object TEXT_DAVINCI_EDIT_001 : ModelType("text-davinci-edit-001", EncodingType.P50K_EDIT, 3000)
  object CODE_DAVINCI_EDIT_001 : ModelType("code-davinci-edit-001", EncodingType.P50K_EDIT, 3000)

  // embeddings
  object TEXT_EMBEDDING_ADA_002 : ModelType("text-embedding-ada-002", CL100K_BASE, 8191)

  // old embeddings
  object TEXT_SIMILARITY_DAVINCI_001 : ModelType("text-similarity-davinci-001", R50K_BASE, 2046)
  object TEXT_SIMILARITY_CURIE_001 : ModelType("text-similarity-curie-001", R50K_BASE, 2046)
  object TEXT_SIMILARITY_BABBAGE_001 : ModelType("text-similarity-babbage-001", R50K_BASE, 2046)
  object TEXT_SIMILARITY_ADA_001 : ModelType("text-similarity-ada-001", R50K_BASE, 2046)
  object TEXT_SEARCH_DAVINCI_DOC_001 : ModelType("text-search-davinci-doc-001", R50K_BASE, 2046)
  object TEXT_SEARCH_CURIE_DOC_001 : ModelType("text-search-curie-doc-001", R50K_BASE, 2046)
  object TEXT_SEARCH_BABBAGE_DOC_001 : ModelType("text-search-babbage-doc-001", R50K_BASE, 2046)
  object TEXT_SEARCH_ADA_DOC_001 : ModelType("text-search-ada-doc-001", R50K_BASE, 2046)
  object CODE_SEARCH_BABBAGE_CODE_001 : ModelType("code-search-babbage-code-001", R50K_BASE, 2046)
  object CODE_SEARCH_ADA_CODE_001 : ModelType("code-search-ada-code-001", R50K_BASE, 2046)


  class FineTunedModel(
    name: String,
    val baseModel: ModelType,
  ) : ModelType(
    name = name,
    encodingType = baseModel.encodingType,
    maxContextLength = baseModel.maxContextLength,
    tokensPerMessage = baseModel.tokensPerMessage,
    tokensPerName = baseModel.tokensPerName,
    tokenPadding = baseModel.tokenPadding,
  )

  /**
   * Currently as of September 2023,
   * [ModelType] has only implementations for OpenAI.
   * Porting this class to other providers will hopefully be addressed in another PR.
   * Meanwhile, [ModelType.TODO] serves as a placeholder.
   */
  class TODO(name: String) : ModelType(name = name, encodingType = EncodingType.CL100K_BASE, maxContextLength = 2048)

  inline val encoding: Encoding
    inline get() = encodingType.encoding

}
