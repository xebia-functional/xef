package com.xebia.functional.xef.llm.models

import ai.xef.openai.OpenAIModel
import com.xebia.functional.tokenizer.ModelType

fun <T> OpenAIModel<T>.modelType(forFunctions: Boolean = false): ModelType {
  val stringValue = value()
  val forFunctionsModel = ModelType.functionSpecific.find { forFunctions && it.name == stringValue }
  return forFunctionsModel
    ?: (ModelType.all.find { it.name == stringValue } ?: ModelType.TODO(stringValue))
}
