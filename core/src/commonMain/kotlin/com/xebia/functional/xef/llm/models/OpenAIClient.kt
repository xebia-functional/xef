package com.xebia.functional.xef.llm.models

import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.openai.generated.model.CreateEmbeddingRequestModel
import com.xebia.functional.tokenizer.ModelType
import io.ktor.client.*

fun CreateChatCompletionRequestModel.modelType(forFunctions: Boolean = false): ModelType {
  val stringValue = value
  val forFunctionsModel = ModelType.functionSpecific.find { forFunctions && it.name == stringValue }
  return forFunctionsModel
    ?: (ModelType.all.find { it.name == stringValue } ?: ModelType.TODO(stringValue))
}

fun CreateEmbeddingRequestModel.modelType(): ModelType {
  val stringValue = value
  return ModelType.all.find { it.name == stringValue } ?: ModelType.TODO(stringValue)
}
