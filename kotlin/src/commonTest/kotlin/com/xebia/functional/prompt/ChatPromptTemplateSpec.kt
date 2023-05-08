package com.xebia.functional.prompt

import arrow.core.raise.either
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class ChatPromptTemplateSpec : StringSpec({
    "ChatPromptTemplate format should return the correct formatted type and content of the messages" {
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

        } shouldBeRight
                "System: You are a helpful assistant that translates English to Spanish.\n" +
                "Human: What is your name?"
    }

    "ChatPromptTemplate format with chat messages should return the correct type, role and content of the messages" {
        either {
            val systemPrompt: PromptTemplate<SystemMessage> = PromptTemplate.system(
                PromptTemplate(
                    "{context}", listOf("context")
                )
            )

            val humanPrompt: PromptTemplate<HumanMessage> = PromptTemplate.human(
                PromptTemplate(
                    "{question}", listOf("question")
                )
            )

            val anakinPrompt: PromptTemplate<ChatMessage> = PromptTemplate.chat(
                PromptTemplate(
                    "{firstPhrase}",
                    listOf("firstPhrase")
                ), "Anakin"
            )

            val obiWanPrompt: PromptTemplate<ChatMessage> = PromptTemplate.chat(
                PromptTemplate(
                    "{secondPhrase}",
                    listOf("secondPhrase")
                ), "Obi-Wan"
            )

            val chatPrompt = ChatPromptTemplate(
                listOf(systemPrompt, humanPrompt, anakinPrompt, obiWanPrompt)
            )

            chatPrompt.format(
                mapOf(
                    "firstPhrase" to "YOU TURNED HER AGAINST ME!?",
                    "secondPhrase" to "You have done that yourself.",
                    "question" to "What movie is this conversation from?",
                    "context" to "You are a helpful assistant that helps on guessing movies."
                )
            ).bind()

        } shouldBeRight
                "System: You are a helpful assistant that helps on guessing movies.\n" +
                "Human: What movie is this conversation from?\n" +
                "Anakin: YOU TURNED HER AGAINST ME!?\n" +
                "Obi-Wan: You have done that yourself."
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