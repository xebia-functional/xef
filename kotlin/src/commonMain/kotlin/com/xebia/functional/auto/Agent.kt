package com.xebia.functional.auto

import com.xebia.functional.tools.Tool
import com.xebia.functional.vectorstores.VectorStore

interface Agent {

    fun tools(): List<Tool>

    suspend fun storeResults(vectorStore: VectorStore) {
        tools().forEach { tool ->
            logger.debug { "[${tool.name}] Running" }
            val docs = tool.action()
            if (docs.isNotEmpty()) {
                vectorStore.addDocuments(docs)
                logger.debug { "[${tool.name}] Found and memorized ${docs.size} docs" }
            } else {
                logger.debug { "[${tool.name}] Found no docs" }
            }
        }
    }

    companion object {
        operator fun invoke(vararg tool: Tool): Agent = object : Agent {
            override fun tools(): List<Tool> = tool.toList()
        }
    }
}
