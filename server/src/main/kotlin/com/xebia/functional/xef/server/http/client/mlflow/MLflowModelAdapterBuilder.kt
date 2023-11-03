package com.xebia.functional.xef.server.http.client.mlflow

import com.xebia.functional.xef.server.http.client.OpenAIPathType

class MLflowModelAdapterBuilder {

  private var pathTypeMap: Map<String, OpenAIPathType> = LinkedHashMap()

  fun addToPath(path: String, pathType: OpenAIPathType) {
    this.pathTypeMap += mapOf(path to pathType)
  }

  internal fun build(): MLflowModelAdapter = MLflowModelAdapter(pathTypeMap)
}
