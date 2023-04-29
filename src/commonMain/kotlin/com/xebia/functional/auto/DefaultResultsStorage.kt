package com.xebia.functional.auto

import com.xebia.functional.vectorstores.VectorStore

class DefaultResultsStorage(
    private val vectorStore: VectorStore
) {

    suspend fun add(task: Task) {
        vectorStore.addTexts(listOf(Task.toJson(task)))
    }

    suspend fun query(query: Objective, topResultsNum: Int): List<Task> {
        return vectorStore.similaritySearch(query.value, topResultsNum).map {
            Task.fromJson(it.content)
        }
    }

}
