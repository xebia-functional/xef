package com.xebia.functional.chains

import arrow.core.raise.either
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.Document
import com.xebia.functional.embeddings.Embedding
import com.xebia.functional.vectorstores.DocumentVectorId
import com.xebia.functional.vectorstores.VectorStore
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class VectorQAChainSpec : StringSpec({
    val outputVariable = "answer"
    val numOfDocs = 10

    "Run should return the answer from the LLMChain" {
        resourceScope {
            either {
                val vectorStore = testVectorStore
                val chain = VectorQAChain(testLLM, vectorStore, numOfDocs, outputVariable)
                chain.run("What do you think?").bind()
            } shouldBeRight testOutputIDK
        }
    }

    "Run should return the answer from the LLMChain when using question explicitly in the inputs" {
        resourceScope {
            either {
                val vectorStore = testVectorStore
                val chain = VectorQAChain(testLLM, vectorStore, numOfDocs, outputVariable)
                chain.run(mapOf("question" to "What do you think?")).bind()
            } shouldBeRight testOutputIDK
        }
    }

    "Run should return the answer from the LLMChain when the input is more than one" {
        resourceScope {
            either {
                val vectorStore = testVectorStore
                val chain = VectorQAChain(testLLM, vectorStore, numOfDocs, outputVariable)
                chain.run(mapOf("question" to "What do you think?", "foo" to "bla bla bla")).bind()
            } shouldBeRight testOutputIDK
        }
    }

    "Run should fail with an InvalidChainInputsError if the inputs don't match the expected" {
        resourceScope {
            either {
                val vectorStore = testVectorStore
                val chain = VectorQAChain(testLLM, vectorStore, numOfDocs, outputVariable)
                chain.run(mapOf("foo" to "What do you think?")).bind()
            } shouldBeLeft
                    Chain.InvalidInputs(
                        "The provided inputs: {foo} do not match with chain's inputs: {question}")
        }
    }
})

val testVectorStore = object : VectorStore {
    override suspend fun addTexts(texts: List<String>): List<DocumentVectorId> = TODO()

    override suspend fun addDocuments(documents: List<Document>): List<DocumentVectorId> = TODO()

    override suspend fun similaritySearch(query: String, limit: Int): List<Document> =
        docsList.map { it.value }

    override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<Document> = TODO()
}

val docsList = mapOf(
    UUID.generateUUID() to Document("foo foo foo"),
    UUID.generateUUID() to Document("bar bar bar"),
    UUID.generateUUID() to Document("baz baz baz")
)
