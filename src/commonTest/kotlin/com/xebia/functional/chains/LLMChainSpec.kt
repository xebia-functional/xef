package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.raise.either
import com.xebia.functional.llm.openai.CompletionChoice
import com.xebia.functional.llm.openai.CompletionRequest
import com.xebia.functional.llm.openai.EmbeddingRequest
import com.xebia.functional.llm.openai.EmbeddingResult
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LLMChainSpec : StringSpec({
    "LLMChain should return a prediction with just the output" {
        val template = "Tell me {foo}."
        either {
            val prompt = PromptTemplate(template, listOf("foo"))
            val chain = LLMChain(llm, prompt, "davinci", "testing", false, 1, 0.0, true)
            chain.run("a joke").bind()
        } shouldBe Either.Right(
            mapOf("answer" to "I'm not good at jokes")
        )
    }

    "LLMChain should return a prediction with both output and inputs" {
        val template = "Tell me {foo}."
        either {
            val prompt = PromptTemplate(template, listOf("foo"))
            val chain = LLMChain(llm, prompt, "davinci", "testing", false, 1, 0.0, false)
            chain.run("a joke").bind()
        } shouldBe Either.Right(
            mapOf("foo" to "a joke", "answer" to "I'm not good at jokes")
        )
    }

    "LLMChain should return a prediction with a more complex template" {
        val template = "My name is {name} and I'm {age} years old"
        either {
            val prompt = PromptTemplate(template, listOf("name", "age"))
            val chain = LLMChain(llm, prompt, "davinci", "testing", false, 1, 0.0, false)
            chain.run(mapOf("age" to "28", "name" to "foo")).bind()
        } shouldBe Either.Right(
            mapOf("age" to "28", "name" to "foo", "answer" to "Hello there! Nice to meet you foo")
        )
    }

    "LLMChain should fail when inputs are not the expected ones from the PromptTemplate" {
        val template = "My name is {name} and I'm {age} years old"
        either {
            val prompt = PromptTemplate(template, listOf("name", "age"))
            val chain = LLMChain(llm, prompt, "davinci", "testing", false, 1, 0.0, false)
            chain.run(mapOf("age" to "28", "brand" to "foo")).bind()
        } shouldBe Either.Left(
            Chain.InvalidInputs("The provided inputs: {age}, {brand} do not match with chain's inputs: {name}, {age}")
        )
    }

    "LLMChain should fail when using just one input but expecting more" {
        val template = "My name is {name} and I'm {age} years old"
        either {
            val prompt = PromptTemplate(template, listOf("name", "age"))
            val chain = LLMChain(llm, prompt, "davinci", "testing", false, 1, 0.0, false)
            chain.run("foo").bind()
        } shouldBe Either.Left(
            Chain.InvalidInputs("The expected inputs are more than one: {name}, {age}")
        )
    }
})

val llm = object : OpenAIClient {
    override suspend fun createCompletion(request: CompletionRequest): List<CompletionChoice> =
        when(request.prompt) {
            "Tell me a joke." ->
                listOf(CompletionChoice("I'm not good at jokes", 1, "foo"))
            "My name is foo and I'm 28 years old" ->
                listOf(CompletionChoice("Hello there! Nice to meet you foo", 1, "foo"))
            else -> listOf(CompletionChoice("foo", 1, "bar"))
        }

    override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult = TODO()
}
