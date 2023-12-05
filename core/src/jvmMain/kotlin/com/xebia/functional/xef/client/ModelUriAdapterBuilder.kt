package com.xebia.functional.xef.client

class ModelUriAdapterBuilder {

  private var pathMap: Map<OpenAIPathType, Map<String, String>> = LinkedHashMap()

  fun setPathMap(pathMap: Map<OpenAIPathType, Map<String, String>>) {
    this.pathMap = pathMap
  }

  fun addToPath(path: OpenAIPathType, vararg modelUriPaths: Pair<String, String>) {
    val newPathTypeMap = mapOf(*modelUriPaths.map { Pair(it.first, it.second) }.toTypedArray())
    this.pathMap += mapOf(path to newPathTypeMap)
  }

  internal fun build(): ModelUriAdapter = ModelUriAdapter(pathMap)
}
