package com.xebia.functional.prompt.chat

import com.xebia.functional.prompt.PromptTemplate

interface MessagePromptTemplate {
    val promptTemplate: PromptTemplate

    suspend fun format(variables: Map<String, String>): Message
}

interface HumanMessagePromptTemplate : MessagePromptTemplate {
    companion object {
        operator fun invoke(
            promptTemplate: PromptTemplate
        ): HumanMessagePromptTemplate = object : HumanMessagePromptTemplate {
            override val promptTemplate: PromptTemplate = promptTemplate

            override suspend fun format(variables: Map<String, String>): HumanMessage =
                HumanMessage(promptTemplate.format(variables))
        }
    }
}

interface SystemMessagePromptTemplate : MessagePromptTemplate {
    companion object {
        operator fun invoke(
            promptTemplate: PromptTemplate
        ): SystemMessagePromptTemplate = object : SystemMessagePromptTemplate {
            override val promptTemplate: PromptTemplate = promptTemplate

            override suspend fun format(variables: Map<String, String>): SystemMessage =
                SystemMessage(promptTemplate.format(variables))
        }
    }
}

interface AIMessagePromptTemplate : MessagePromptTemplate {
    companion object {
        operator fun invoke(
            promptTemplate: PromptTemplate
        ): AIMessagePromptTemplate = object : AIMessagePromptTemplate {
            override val promptTemplate: PromptTemplate = promptTemplate

            override suspend fun format(variables: Map<String, String>): AIMessage =
                AIMessage(promptTemplate.format(variables))
        }
    }
}

interface ChatMessagePromptTemplate : MessagePromptTemplate {
    val role: String

    companion object {
        operator fun invoke(
            promptTemplate: PromptTemplate, role: String
        ): ChatMessagePromptTemplate = object : ChatMessagePromptTemplate {
            override val promptTemplate: PromptTemplate = promptTemplate
            override val role: String = role

            override suspend fun format(variables: Map<String, String>): ChatMessage =
                ChatMessage(promptTemplate.format(variables), role)
        }
    }
}
