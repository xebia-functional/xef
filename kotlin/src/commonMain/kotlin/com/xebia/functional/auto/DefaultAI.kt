package com.xebia.functional.auto

import arrow.core.raise.Raise
import arrow.core.raise.catch
import com.xebia.functional.auto.serialization.JsonSchema
import com.xebia.functional.auto.serialization.JsonType
import com.xebia.functional.auto.serialization.jsonLiteral
import com.xebia.functional.auto.serialization.jsonType
import com.xebia.functional.llm.openai.ChatCompletionRequest
import com.xebia.functional.llm.openai.ChatCompletionResponse
import com.xebia.functional.llm.openai.Message
import com.xebia.functional.llm.openai.Role
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.sample(): JsonElement {
    val properties = elementDescriptors.associate {
        it.serialName to when (kind.jsonType) {
            JsonType.ARRAY -> JsonArray(listOf(it.sample()))
            JsonType.NUMBER -> JsonUnquotedLiteral("<infer number>")
            JsonType.STRING -> JsonUnquotedLiteral("<infer string>")
            JsonType.BOOLEAN -> JsonUnquotedLiteral("<infer true | false>")
            JsonType.OBJECT -> it.sample()
            JsonType.OBJECT_SEALED -> it.sample()
            JsonType.OBJECT_MAP -> it.sample()
        }
    }
    return JsonObject(properties)
}

class DefaultAI(override val config: Config, val raise: Raise<AIError>) : AI {

    override fun raise(r: AIError): Nothing {
        raise.raise(r)
    }

    override suspend operator fun <A> invoke(
        prompt: String, serializationConfig: SerializationConfig<A>
    ): A {
        val augmentedPrompt = """
                |Objective: $prompt
                |Instructions: Use the following JSON schema to produce the result on json format
                |JSON Schema:${serializationConfig.jsonSchema}
                |Example response: ${serializationConfig.descriptor.sample()}.
                |Return exactly the JSON response and nothing else
            """.trimMargin()
        val result = if (config.auto) {
            logger.debug { "Solving objective in auto reasoning mode" }
            val resolutionContext: Solution = solveObjective(prompt, 5)
            val promptWithContext = """
                    |${resolutionContext.result}
                    |
                    |Given this information solve the objective:
                    |
                    |$augmentedPrompt
                """.trimMargin()
            openAIChatCall(promptWithContext)
        } else {
            logger.debug { "Solving objective by direct call" }
            openAIChatCall(augmentedPrompt)
        }
        return catch({
            json.decodeFromString(serializationConfig.deserializationStrategy, result)
        }) { e ->
            val fixJsonPrompt = """
                    |Result: $result
                    |Exception: ${e.message}
                    |Objective: $prompt
                    |Instructions: Use the following JSON schema to produce the result on valid json format avoiding the exception.
                    |JSON Schema:${serializationConfig.jsonSchema}
                    """.trimMargin()
            logger.debug { "Attempting to Fix JSON due to error: ${e.message}" }

            //here we should retry and handle errors, when we are executing the `ai` function again it might fail and it eventually crashes
            //we should handle this and retry
            invoke(fixJsonPrompt, serializationConfig).also { logger.debug { "Fixed JSON: $it" } }
        }
    }

    tailrec suspend fun Raise<AIError>.solveObjective(
        task: String, maxAttempts: Int
    ): Solution = if (maxAttempts <= 0) {
        raise(AIError("Exceeded maximum attempts"))
    } else {
        val solution: Solution = AI<Solution>(this@DefaultAI, task).bind()
        if (solution.accomplishesObjective) {
            solution
        } else {
            solveObjective(solution.result, maxAttempts - 1)
        }
    }

    private suspend fun openAIChatCall(
        promptWithContext: String
    ): String {
        val res = chatCompletionResponse(promptWithContext, "gpt-3.5-turbo", "AI_Value_Generator")
        val msg = res.choices.firstOrNull()?.message?.content
        requireNotNull(msg) { "No message found in result: $res" }
        return msg
    }

    private suspend fun chatCompletionResponse(
        prompt: String, model: String, user: String
    ): ChatCompletionResponse {
        val completionRequest = ChatCompletionRequest(
            model = model, messages = listOf(Message(Role.system.name, prompt, user)), user = user
        )
        return config.openAIClient.createChatCompletion(completionRequest)
    }
}
