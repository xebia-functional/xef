package com.xebia.functional.xef.llm.models

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.openapi.CreateChatCompletionRequest
import com.xebia.functional.xef.openapi.CreateEmbeddingRequest

fun CreateChatCompletionRequest.Model.modelType(forFunctions: Boolean = false): ModelType {
  val stringValue = value
  val forFunctionsModel = ModelType.functionSpecific.find { forFunctions && it.name == stringValue }
  return forFunctionsModel
    ?: (ModelType.all.find { it.name == stringValue } ?: ModelType.TODO(stringValue))
}

fun CreateEmbeddingRequest.Model.modelType(): ModelType {
  val stringValue = value
  return ModelType.all.find { it.name == stringValue } ?: ModelType.TODO(stringValue)
}
