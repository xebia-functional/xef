package com.xebia.functional.prompt

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either

data class InvalidInputs(val reason: String)

fun Raise<InvalidInputs>.ChatPromptTemplate(
    promptMessages: List<PromptTemplate<out Message>>
): ChatPromptTemplate {
    val inputKeys: List<String> = promptMessages.flatMap { it.inputKeys }
    return ChatPromptTemplate(inputKeys, promptMessages)
}

interface ChatPromptTemplate {
    val promptMessages: List<PromptTemplate<out Message>>
    val inputKeys: List<String>

    suspend fun format(inputs: Map<String, String>): Either<InvalidInputs, String>

    suspend fun formatMessages(inputs: Map<String, String>): Either<InvalidInputs, List<Message>>

    companion object {

        operator fun invoke(
            inputKeys: List<String>,
            promptMessages: List<PromptTemplate<out Message>>
        ): ChatPromptTemplate = object : ChatPromptTemplate {
            override val promptMessages: List<PromptTemplate<out Message>> = promptMessages
            override val inputKeys: List<String> = inputKeys

            override suspend fun format(inputs: Map<String, String>): Either<InvalidInputs, String> =
                either {
                    val messages: List<Message> = formatMessages(inputs).bind()
                    messages.joinToString(separator = "\n") { it.content }
                }

            override suspend fun formatMessages(
                inputs: Map<String, String>
            ): Either<InvalidInputs, List<Message>> =
                either {
                    val allInputs: Map<String, String> =
                        PromptTemplate.mergePartialAndUserVariables(inputs, inputKeys)

                    promptMessages.map { prompt ->
                        val inputValues: Map<String, String> = createInputs(allInputs, prompt.inputKeys)
                        prompt.format(inputValues)
                    }
                }

            private fun Raise<InvalidInputs>.createInputs(
                inputs: Map<String, String>, inputKeys: List<String>
            ): Map<String, String> =
                inputKeys.associateWith { inputKey ->
                    inputs[inputKey] ?: raise(
                        InvalidInputs("The provided inputs: " +
                                inputs.keys.joinToString(", ") { "{$it}" } +
                                " do not match with prompt's inputs: " +
                                inputKeys.joinToString(", ") { "{$it}" })
                    )
                }
        }
    }
}
