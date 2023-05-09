package com.xebia.functional.tools

import com.xebia.functional.Document
import com.xebia.functional.vectorstores.VectorStore
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging

class Agent(
  val name: String,
  val description: String,
  val action: suspend Agent.() -> List<Document>,
) {
  val logger: KLogger by lazy { KotlinLogging.logger(name) }

  suspend fun storeResults(vectorStore: VectorStore) {
    logger.debug { "[${name}] Running" }
    val docs = action()
    if (docs.isNotEmpty()) {
      vectorStore.addDocuments(docs)
      logger.debug { "[${name}] Found and memorized ${docs.size} docs" }
    } else {
      logger.debug { "[${name}] Found no docs" }
    }
  }
}

suspend fun Array<out Agent>.storeResults(vectorStore: VectorStore) =
  forEach { it.storeResults(vectorStore) }
