package com.xebia.functional.auto

import com.xebia.functional.tools.Tool
import com.xebia.functional.vectorstores.VectorStore
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging

class Agent(private val tools: List<Tool>) {

  constructor(tool: Array<out Tool>) : this(tool.toList())

  val logger: KLogger = KotlinLogging.logger("Agent")

  suspend fun storeResults(vectorStore: VectorStore) {
    tools.forEach { tool ->
      logger.debug { "[${tool.name}] Running" }
      val docs = tool.action(tool)
      if (docs.isNotEmpty()) {
        vectorStore.addDocuments(docs)
        logger.debug { "[${tool.name}] Found and memorized ${docs.size} docs" }
      } else {
        logger.debug { "[${tool.name}] Found no docs" }
      }
    }
  }
}
