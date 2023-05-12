package com.xebia.functional.chains

import arrow.core.raise.either
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.AIError
import com.xebia.functional.embeddings.Embedding
import com.xebia.functional.llm.openai.LLMModel
import com.xebia.functional.vectorstores.VectorStore
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class VectorQAChainSpec :
  StringSpec({
    val outputVariable = "answer"
    val numOfDocs = 10
    val model = LLMModel.GPT_3_5_TURBO

    "Run should return the answer from the LLMChain" {
      resourceScope {
        either {
          val vectorStore = testVectorStore
          val chain = VectorQAChain(testLLM, model, vectorStore, numOfDocs, outputVariable)
          chain.run("What do you think?").bind()
        } shouldBeRight testOutputIDK
      }
    }

    "Run should return the answer from the LLMChain when using question explicitly in the inputs" {
      resourceScope {
        either {
          val vectorStore = testVectorStore
          val chain = VectorQAChain(testLLM, model, vectorStore, numOfDocs, outputVariable)
          chain.run(mapOf("question" to "What do you think?")).bind()
        } shouldBeRight testOutputIDK
      }
    }

    "Run should return the answer from the LLMChain when the input is more than one" {
      resourceScope {
        either {
          val vectorStore = testVectorStore
          val chain = VectorQAChain(testLLM, model, vectorStore, numOfDocs, outputVariable)
          chain.run(mapOf("question" to "What do you think?", "foo" to "bla bla bla")).bind()
        } shouldBeRight testOutputIDK
      }
    }

    "Run should fail with an InvalidChainInputsError if the inputs don't match the expected" {
      resourceScope {
        either {
          val vectorStore = testVectorStore
          val chain = VectorQAChain(testLLM, model, vectorStore, numOfDocs, outputVariable)
          chain.run(mapOf("foo" to "What do you think?")).bind()
        } shouldBeLeft
          AIError.Chain.InvalidInputs(
            "The provided inputs: {foo} do not match with chain's inputs: {question}"
          )
      }
    }
  })

val testVectorStore =
  object : VectorStore {
    override suspend fun addTexts(texts: List<String>) = TODO()

    override suspend fun similaritySearch(query: String, limit: Int): List<String> = docsList

    override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<String> =
      TODO()
  }

val docsList = listOf("foo foo foo", "bar bar bar", "baz baz baz")
