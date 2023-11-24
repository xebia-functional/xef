package com.xebia.functional.xef.llm

import com.xebia.functional.xef.llm.models.ModelID

// sealed modifier temporarily removed as OAI's implementation of tokensFromMessages has to extend
// and override LLM
/*sealed */ interface LLM : AutoCloseable {

  val modelID: ModelID

  @Deprecated("use modelID.value instead", replaceWith = ReplaceWith("modelID.value"))
  val name
    get() = modelID.value

  /**
   * Copies this instance and uses [modelID] for the new instances' [LLM.modelID]. Has to return the
   * most specific type of this instance!
   */
  fun copy(modelID: ModelID): LLM

  override fun close() = Unit
}
