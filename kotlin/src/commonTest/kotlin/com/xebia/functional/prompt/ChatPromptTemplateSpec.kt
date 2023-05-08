package com.xebia.functional.prompt

import arrow.core.raise.either
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class ChatPromptTemplateSpec : StringSpec({
    "ChatPromptTemplate format should return the formatted content of the messages" {
        either {
            val systemPrompt: PromptTemplate<SystemMessage> = PromptTemplate.system(
                PromptTemplate(
                    "You are a helpful assistant that translates {input_language} to {output_language}.",
                    listOf("input_language", "output_language")
                )
            )

            val humanPrompt: PromptTemplate<HumanMessage> = PromptTemplate.human(
                PromptTemplate(
                    "{text}",
                    listOf("text")
                )
            )

            val chatPrompt = ChatPromptTemplate(
                listOf(systemPrompt, humanPrompt)
            )

            chatPrompt.format(
                mapOf(
                    "input_language" to "English",
                    "output_language" to "Spanish",
                    "text" to "What is your name?"
                    )
            ).bind()

        } shouldBeRight "You are a helpful assistant that translates English to Spanish.\n" +
                "What is your name?"
    }

    "ChatPromptTemplate formatMessages should return the formatted list of messages" {
        either {
            val systemPrompt: PromptTemplate<SystemMessage> = PromptTemplate.system(
                PromptTemplate(
                    "You are a helpful assistant that translates {input_language} to {output_language}.",
                    listOf("input_language", "output_language")
                )
            )

            val humanPrompt: PromptTemplate<HumanMessage> = PromptTemplate.human(
                PromptTemplate(
                    "{text}",
                    listOf("text")
                )
            )

            val chatPrompt = ChatPromptTemplate(
                listOf(systemPrompt, humanPrompt)
            )

            chatPrompt.formatMessages(
                mapOf(
                    "input_language" to "English",
                    "output_language" to "Spanish",
                    "text" to "What is your name?"
                )
            ).bind()

        } shouldBeRight listOf(
            SystemMessage("You are a helpful assistant that translates English to Spanish."),
            HumanMessage("What is your name?")
        )
    }
})