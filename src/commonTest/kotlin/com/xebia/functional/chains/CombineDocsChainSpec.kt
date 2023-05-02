package com.xebia.functional.chains

import arrow.core.raise.either
import com.xebia.functional.prompt.PromptTemplate
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class CombineDocsChainSpec : StringSpec({
    "LLMChain should return a prediction with just the output" {
        val template = "Tell me {foo}."
        either {
            val promptTemplate = PromptTemplate(template, listOf("foo"))
            val chain = LLMChain(llm, promptTemplate, outputVariable = "answer")
            chain.run("a joke").bind()
        } shouldBeRight mapOf("answer" to "I'm not good at jokes")
    }

    "LLMChain should return a prediction with both output and inputs" {
        val template = "Tell me {foo}."
        either {
            val prompt = PromptTemplate(template, listOf("foo"))
            val chain = LLMChain(llm, prompt, outputVariable = "answer", chainOutput = Chain.ChainOutput.InputAndOutput)
            chain.run("a joke").bind()
        } shouldBeRight mapOf("foo" to "a joke", "answer" to "I'm not good at jokes")
    }

    "LLMChain should return a prediction with a more complex template" {
        val template = "My name is {name} and I'm {age} years old"
        either {
            val prompt = PromptTemplate(template, listOf("name", "age"))
            val chain = LLMChain(llm, prompt, outputVariable = "answer", chainOutput = Chain.ChainOutput.InputAndOutput)
            chain.run(mapOf("age" to "28", "name" to "foo")).bind()
        } shouldBeRight mapOf("age" to "28", "name" to "foo", "answer" to "Hello there! Nice to meet you foo")
    }

    "LLMChain should fail when inputs are not the expected ones from the PromptTemplate" {
        val template = "My name is {name} and I'm {age} years old"
        either {
            val prompt = PromptTemplate(template, listOf("name", "age"))
            val chain = LLMChain(llm, prompt, outputVariable = "answer", chainOutput = Chain.ChainOutput.InputAndOutput)
            chain.run(mapOf("age" to "28", "brand" to "foo")).bind()
        } shouldBeLeft
                Chain.InvalidInputs(
                    "The provided inputs: {age}, {brand} do not match with chain's inputs: {name}, {age}")
    }

    "LLMChain should fail when using just one input but expecting more" {
        val template = "My name is {name} and I'm {age} years old"
        either {
            val prompt = PromptTemplate(template, listOf("name", "age"))
            val chain = LLMChain(llm, prompt, outputVariable = "answer", chainOutput = Chain.ChainOutput.InputAndOutput)
            chain.run("foo").bind()
        } shouldBeLeft Chain.InvalidInputs("The expected inputs are more than one: {name}, {age}")
    }
})
