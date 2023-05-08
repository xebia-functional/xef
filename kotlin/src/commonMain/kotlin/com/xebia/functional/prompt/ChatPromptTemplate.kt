package com.xebia.functional.prompt

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull

data class InvalidInputs(val reason: String)

interface ChatPromptTemplate {
    val promptMessages: List<PromptTemplate<out Message>>
    val inputKeys: List<String>

    suspend fun format(inputs: Map<String, String>): Either<InvalidInputs, String>

    suspend fun formatMessages(inputs: Map<String, String>): Either<InvalidInputs, List<Message>>
}

suspend fun ChatPromptTemplate(
    promptMessages: List<PromptTemplate<out Message>>
): ChatPromptTemplate = object : ChatPromptTemplate {
    override val promptMessages: List<PromptTemplate<out Message>> = promptMessages
    override val inputKeys: List<String> = promptMessages.flatMap { it.inputKeys }

    override suspend fun format(inputs: Map<String, String>): Either<InvalidInputs, String> =
        either {
            val messages: List<Message> = formatMessages(inputs).bind()
            messages.joinToString(separator = "\n") { message ->
                when (message) {
                    is HumanMessage -> message.format()
                    is AIMessage -> message.format()
                    is SystemMessage -> message.format()
                    is ChatMessage -> message.format()
                }
            }
        }

    override suspend fun formatMessages(inputs: Map<String, String>): Either<InvalidInputs, List<Message>> =
        either {
            promptMessages.map { prompt ->
                val inputValues: Map<String, String> = createInputs(inputs, prompt.inputKeys)
                prompt.format(inputValues)
            }
        }

    private fun Raise<InvalidInputs>.createInputs(
        inputs: Map<String, String>, inputKeys: List<String>
    ): Map<String, String> =
        inputKeys.associateWith { inputKey ->
            ensureNotNull(inputs[inputKey]) {
                InvalidInputs("The provided inputs: " +
                        inputs.keys.joinToString(", ") { "{$it}" } +
                        " do not match with prompt's inputs: " +
                        inputKeys.joinToString(", ") { "{$it}" })
            }
        }
}

private fun Message.format(): String =
    "${type().capitalizedName()}: $content"

private fun ChatMessage.format(): String =
    "${this.role}: $content"

private fun Type.capitalizedName() =
    name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
