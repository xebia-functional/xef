package com.xebia.functional.persistence

import arrow.fx.stm.TMap
import arrow.fx.stm.TVar
import arrow.fx.stm.atomically
import com.xebia.functional.embeddings.Embedding
import com.xebia.functional.embeddings.Embeddings
import com.xebia.functional.embeddings.cosineSimilarity
import com.xebia.functional.llm.openai.EmbeddingModel
import com.xebia.functional.llm.openai.RequestConfig
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class LocalPersistence<Document> private constructor(
    private val elements: TMap<UUID, Document>
): PersistenceUnique<Document, UUID> {

    companion object {
        suspend operator fun <Document> invoke(): LocalPersistence<Document> =
            LocalPersistence(TMap.new())
    }

    override suspend fun addDocument(doc: Document): UUID = atomically {
        UUID.generateUUID().also { elements.insert(it, doc) }
    }

    override suspend fun documentById(id: UUID): Document? = atomically { elements[id] }
}

class LocalPersistenceSimilarity<Document>(
    private val conversion: (Document) -> String,
    private val embeddings: Embeddings,
    private val elements: TVar<Map<UUID, Pair<Document, Embedding>>>
): PersistenceSimilarity<Document, UUID>, PersistenceUnique<Document, UUID> {

    companion object {
        suspend operator fun <Document> invoke(
            conversion: (Document) -> String,
            embeddings: Embeddings
        ): LocalPersistenceSimilarity<Document> =
            LocalPersistenceSimilarity(conversion, embeddings, TVar.new(emptyMap()))

        suspend operator fun invoke(
            embeddings: Embeddings
        ): LocalPersistenceSimilarity<String> =
            LocalPersistenceSimilarity({ it }, embeddings)
    }

    private val requestConfig =
        RequestConfig(EmbeddingModel.TextEmbeddingAda002, RequestConfig.Companion.User("user"))

    override suspend fun addDocument(doc: Document): UUID = addDocuments(listOf(doc)).first()

    override suspend fun addDocuments(docs: List<Document>): List<UUID> {
        val uuids = docs.map { UUID.generateUUID() }
        val embs = embeddings.embedDocuments(docs.map(conversion), chunkSize = null, requestConfig = requestConfig)
        atomically {
            elements.modify { it + uuids.zip(docs.zip(embs)).toMap() }
        }
        return uuids
    }

    override suspend fun documentById(id: UUID): Document? = atomically { elements.read()[id]?.first }

    override suspend fun similaritySearch(doc: Document, limit: Int?): List<Document> {
        val queryEmbedding = embeddings.embedQuery(conversion(doc), requestConfig = requestConfig).firstOrNull()
            ?: return emptyList()

        val results = atomically {
            elements.read().map { (_, de) ->
                de.first to de.second.cosineSimilarity(queryEmbedding)
            }
        }.sortedByDescending { (_, similarity) -> similarity }

        val limited = if (limit != null) results.take(limit) else results

        return limited.map { it.first }
    }

}
