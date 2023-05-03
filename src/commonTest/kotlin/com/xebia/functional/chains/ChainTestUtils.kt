package com.xebia.functional.chains

import com.xebia.functional.llm.openai.ChatCompletionRequest
import com.xebia.functional.llm.openai.ChatCompletionResponse
import com.xebia.functional.llm.openai.CompletionChoice
import com.xebia.functional.llm.openai.CompletionRequest
import com.xebia.functional.llm.openai.EmbeddingRequest
import com.xebia.functional.llm.openai.EmbeddingResult
import com.xebia.functional.llm.openai.OpenAIClient

val testLLM = object : OpenAIClient {
    override suspend fun createCompletion(request: CompletionRequest): List<CompletionChoice> =
        when(request.prompt) {
            "Tell me a joke." ->
                listOf(CompletionChoice("I'm not good at jokes", 1, "foo"))
            "My name is foo and I'm 28 years old" ->
                listOf(CompletionChoice("Hello there! Nice to meet you foo", 1, "foo"))
            testTemplateFormatted -> listOf(CompletionChoice("I don't know", 1, "foo"))
            testTemplateInputsFormatted -> listOf(CompletionChoice("Two inputs, right?", 1, "foo"))
            testQATemplateFormatted -> listOf(CompletionChoice("I don't know", 1, "foo"))
            else -> listOf(CompletionChoice("foo", 1, "bar"))
        }

    override suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse =
        TODO()

    override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult =
        TODO()
}

val testContext = """foo foo foo
  |bar bar bar
  |baz baz baz""".trimMargin()

val testContextOutput = mapOf("context" to testContext)

val testTemplate = """From the following context:
    |
    |{context}
    |
    |try to answer the following question: {question}""".trimMargin()

val testTemplateInputs = """From the following context:
    |
    |{context}
    |
    |I want to say: My name is {name} and I'm {age} years old""".trimMargin()

val testTemplateFormatted = """From the following context:
    |
    |foo foo foo
    |bar bar bar
    |baz baz baz
    |
    |try to answer the following question: What do you think?""".trimMargin()

val testTemplateInputsFormatted = """From the following context:
    |
    |foo foo foo
    |bar bar bar
    |baz baz baz
    |
    |I want to say: My name is Scala and I'm 28 years old""".trimMargin()

val testQATemplateFormatted = """
    |Use the following pieces of context to answer the question at the end. If you don't know the answer, just say that you don't know, don't try to make up an answer.
    |
    |foo foo foo
    |bar bar bar
    |baz baz baz
    |
    |Question: What do you think?
    |Helpful Answer:""".trimMargin()

val testOutputIDK = mapOf("answer" to "I don't know")
val testOutputInputs = mapOf("answer" to "Two inputs, right?")
