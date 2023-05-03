package com.xebia.functional.chains

import arrow.core.raise.either
import com.xebia.functional.Document
import com.xebia.functional.prompt.PromptTemplate
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class CombineDocsChainSpec : StringSpec({
    val documentVariableName = "context"
    val outputVariable = "answer"

    "Combine should return all the documents properly combined" {
        either {
            val promptTemplate = PromptTemplate(testTemplate, listOf("context", "question"))
            val docs = listOf(Document("foo foo foo"), Document("bar bar bar"), Document("baz baz baz"))
            val chain = CombineDocsChain(testLLM, promptTemplate, docs, documentVariableName, outputVariable)
            chain.combine(docs)
        } shouldBeRight testContextOutput
    }

    "Run should return the proper LLMChain response with one input" {
        either {
            val promptTemplate = PromptTemplate(testTemplate, listOf("context", "question"))
            val docs = listOf(Document("foo foo foo"), Document("bar bar bar"), Document("baz baz baz"))
            val chain = CombineDocsChain(testLLM, promptTemplate, docs, documentVariableName, outputVariable)
            chain.run("What do you think?").bind()
        } shouldBeRight testOutputIDK
    }

    "Run should return the proper LLMChain response with more than one input" {
        either {
            val promptTemplate = PromptTemplate(testTemplateInputs, listOf("context", "name", "age"))
            val docs = listOf(Document("foo foo foo"), Document("bar bar bar"), Document("baz baz baz"))
            val chain = CombineDocsChain(
                testLLM, promptTemplate, docs, documentVariableName, outputVariable, Chain.ChainOutput.InputAndOutput)
            chain.run(mapOf("name" to "Scala", "age" to "28")).bind()
        } shouldBeRight testOutputInputs + mapOf("context" to testContext, "name" to "Scala", "age" to "28")
    }

    "Run should fail with a InvalidCombineDocumentsChainError if the inputs don't match the expected" {
        either {
            val promptTemplate = PromptTemplate(testTemplateInputs, listOf("context", "name", "age"))
            val docs = listOf(Document("foo foo foo"), Document("bar bar bar"), Document("baz baz baz"))
            val chain = CombineDocsChain(
                testLLM, promptTemplate, docs, documentVariableName, outputVariable, Chain.ChainOutput.InputAndOutput)
            chain.run(mapOf("name" to "Scala", "city" to "Seattle")).bind()
        } shouldBeLeft
                Chain.InvalidInputs(
                    "The provided inputs: {name}, {city} do not match with chain's inputs: {name}, {age}")
    }
})
