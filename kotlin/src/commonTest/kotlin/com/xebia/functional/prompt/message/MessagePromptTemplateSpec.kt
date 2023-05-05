package com.xebia.functional.prompt.message

import arrow.core.raise.either
import com.xebia.functional.prompt.PromptTemplate
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class MessagePromptTemplateSpec : StringSpec({
    "HumanMessagePromptTemplate format should return a HumanMessage" {
        val template = "My name is {name} and I'm {age} years old"
        val variables: Map<String, String> = mapOf("name" to "Charles", "age" to "21")

        either {
            val prompt = PromptTemplate(template, listOf("name", "age"))
            val humanPrompt = HumanMessagePromptTemplate(prompt)
            humanPrompt.format(variables)

        } shouldBeRight HumanMessage("My name is Charles and I'm 21 years old")
    }

    "SystemMessagePromptTemplate format should return a SystemMessage" {
        val template = "{sounds}"
        val variables: Map<String, String> = mapOf("sounds" to "Beep bep")

        either {
            val prompt = PromptTemplate(template, listOf("sounds"))
            val systemPrompt = SystemMessagePromptTemplate(prompt)
            systemPrompt.format(variables)

        } shouldBeRight SystemMessage("Beep bep")
    }

    "AIMessagePromptTemplate format should return a AIMessage" {
        val template = "Hi, I'm an {machine}"
        val variables: Map<String, String> = mapOf("machine" to "AI")

        either {
            val prompt = PromptTemplate(template, listOf("machine"))
            val aiPrompt = AIMessagePromptTemplate(prompt)
            aiPrompt.format(variables)

        } shouldBeRight AIMessage("Hi, I'm an AI")
    }

    "ChatMessagePromptTemplate format should return a ChatMessage" {
        val role = "Yoda"
        val template = "Lost a {action}, master {name} has."
        val variables: Map<String, String> = mapOf("action" to "battle", "name" to "Obi-Wan")

        either {
            val prompt = PromptTemplate(template, listOf("action", "name"))
            val chatPrompt = ChatMessagePromptTemplate(prompt, role)
            chatPrompt.format(variables)

        } shouldBeRight ChatMessage("Lost a battle, master Obi-Wan has.", "Yoda")
    }
})
