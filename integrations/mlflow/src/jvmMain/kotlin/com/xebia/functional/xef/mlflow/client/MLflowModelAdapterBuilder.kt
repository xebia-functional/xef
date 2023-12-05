package com.xebia.functional.xef.mlflow.client

import com.xebia.functional.xef.client.OpenAIPathType

class MLflowModelAdapterBuilder {

  private var pathTypeMap: Map<String, OpenAIPathType> = LinkedHashMap()

  fun setPathMap(pathMap: Map<String, OpenAIPathType>) {
    this.pathTypeMap = pathMap
  }

  fun addToPath(path: String, pathType: OpenAIPathType) {
    this.pathTypeMap += mapOf(path to pathType)
  }

  internal fun build(): MLflowModelAdapter = MLflowModelAdapter(pathTypeMap)
}
