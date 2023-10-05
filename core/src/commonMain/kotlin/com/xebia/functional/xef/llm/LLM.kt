package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.xef.llm.models.ModelID
import com.xebia.functional.xef.llm.models.chat.Message

// sealed modifier temporarily removed as OAI's implementation of tokensFromMessages has to extend
// and override LLM
/*sealed */ interface LLM : AutoCloseable {

  val modelID: ModelID

  @Deprecated("intermediary solution, will be removed in future PR")
  val modelType: ModelType
    get() = when {
      modelID.value.lowercase().startsWith("gpt") -> EncodingType.CL100K_BASE
      modelID.value.lowercase() == "text-embedding-ada-002" -> EncodingType.CL100K_BASE
      else -> EncodingType.P50K_BASE
    }.let { ModelType(it) }

  @Deprecated("use modelID.value instead", replaceWith = ReplaceWith("modelID.value"))
  val name
    get() = modelID.value

  /**
   * Copies this instance and uses [modelType] for [LLM.modelType]. Has to return the most specific
   * type of this instance!
   */
  fun copy(modelID: ModelID): LLM

  @Deprecated("will be moved out of LLM in favor of abstracting former ModelType")
  fun tokensFromMessages(messages: List<Message>): Int = TODO() // intermediary

  @Deprecated("will be moved out of LLM in favor of abstracting former ModelType")
  fun truncateText(text: String, maxTokens: Int): String = TODO() // intermediary

  @Deprecated("will be moved out of LLM in favor of abstracting former ModelType")
  val maxContextLength: Int
    get() = TODO()

  override fun close() = Unit
}

/**
 * intermediary solution
 */
class ModelType(
  val encodingType: EncodingType,
) {
  val encoding get() = encodingType.encoding
}
