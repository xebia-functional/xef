package com.xebia.functional.auto

import com.xebia.functional.tools.Tool
import com.xebia.functional.vectorstores.VectorStore

interface Agent {

    fun tools(): List<Tool>

    suspend fun storeResults(prompt: String, vectorStore: VectorStore) {
        tools().forEach { tool ->
            val docs = tool.action(prompt)
            vectorStore.addDocuments(docs)
        }
    }

    companion object {
        operator fun invoke(vararg tool: Tool): Agent = object : Agent {
            override fun tools(): List<Tool> = tool.toList()
        }
    }
}
