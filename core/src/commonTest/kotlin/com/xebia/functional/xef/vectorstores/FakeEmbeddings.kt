package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig

class FakeEmbeddings : Embeddings {
    override suspend fun embedDocuments(
        texts: List<String>, chunkSize: Int?, requestConfig: RequestConfig
    ): List<Embedding> = emptyList()


    override suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding> = emptyList()
}
