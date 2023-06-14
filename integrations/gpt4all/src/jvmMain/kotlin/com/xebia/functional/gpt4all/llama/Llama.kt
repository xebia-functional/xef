package com.xebia.functional.gpt4all.llama

import com.xebia.functional.gpt4all.Embedding
import com.xebia.functional.gpt4all.EmbeddingRequest
import com.xebia.functional.gpt4all.EmbeddingResponse
import java.nio.file.Path

interface Llama : AutoCloseable {
    val model: LlamaModel

    suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResponse

    companion object {
        operator fun invoke(
            path: Path
        ): Llama = object : Llama {
            override val model: LlamaModel = LlamaModel(path)

            override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResponse {
                val embeddings: List<Embedding> = request.input.map { input ->
                    val result: List<Float> = model.embeddings(input)
                    Embedding(result)
                }
                return EmbeddingResponse(model.name, embeddings)
            }

            override fun close(): Unit =
                model.close()
        }
    }
}
