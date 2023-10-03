package com.xebia.functional.xef.llm

import com.xebia.functional.xef.llm.models.ModelID

sealed interface LLM : AutoCloseable {

  val modelID: ModelID

  val modelType: Nothing
    get() = TODO()

  @Deprecated("use modelID.value instead", replaceWith = ReplaceWith("modelID.value"))
  val name
    get() = modelID.value

  override fun close() = Unit
}
