package com.xebia.functional.chains.contextual

import arrow.core.raise.Raise
import com.xebia.functional.prompt.Config
import com.xebia.functional.prompt.InvalidTemplate
import com.xebia.functional.prompt.PromptTemplate

object ContextualPrompt {
    private val template = """
    |Use the following pieces of context to answer the question at the end. If you don't know the answer, just say that you don't know, don't try to make up an answer.
    |
    |{context}
    |
    |Question: {question}
    |Helpful Answer:""".trimMargin()

    fun Raise<InvalidTemplate>.PromptTemplate(): PromptTemplate =
        PromptTemplate(Config(template, listOf("context", "question")))
}
