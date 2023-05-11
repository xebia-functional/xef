package com.xebia.functional.persistence

interface Persistence<in Document, out Id> {
    suspend fun addDocument(doc: Document): Id

    suspend fun addDocuments(docs: List<Document>): List<Id> =
        docs.map { addDocument(it) }
}

interface PersistenceUnique<Document, Id>: Persistence<Document, Id> {
    suspend fun documentById(id: Id): Document?
}

interface PersistenceSimilarity<Document, Id>: Persistence<Document, Id> {
    suspend fun similaritySearch(doc: Document, limit: Int?): List<Document>
}

interface PersistenceSearch<Document, Id, Query>: Persistence<Document, Id> {
    suspend fun search(query: Query, limit: Int?): List<Document>
}

