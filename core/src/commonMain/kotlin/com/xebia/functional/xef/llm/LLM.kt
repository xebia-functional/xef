package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.xef.llm.models.ModelID
import com.xebia.functional.xef.llm.models.chat.Message

// sealed modifier temporarily removed as OAI's implementation of tokensFromMessages has to extend and override LLM
/*sealed */interface LLM : AutoCloseable {

  val modelID: ModelID

  @Deprecated("use modelID.value instead", replaceWith = ReplaceWith("modelID.value"))
  val name
    get() = modelID.value

  /**
   * Copies this instance and uses [modelType] for [LLM.modelType]. Has to return the most specific
   * type of this instance!
   */
  fun copy(modelType: ModelType): LLM

  fun tokensFromMessages(
    messages: List<Message>
  ): Int { // TODO: naive implementation with magic numbers
    TODO()
  }

  override fun close() = Unit
}
