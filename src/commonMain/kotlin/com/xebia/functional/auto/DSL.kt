package com.xebia.functional.auto

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.fx.coroutines.continuations.ResourceScope
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.KtorOpenAIClient
import com.xebia.functional.vectorstores.LocalVectorStore
import io.github.oshai.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import kotlin.time.ExperimentalTime


suspend inline fun <reified A> ai(prompt: String): A {
    val descriptor = serialDescriptor<A>()
    val jsonSchema = buildJsonSchema(descriptor)
    val augmentedPrompt = """
                |Objective: $prompt
                |Instructions: Use the following JSON schema to produce the result on json format
                |JSON Schema:$jsonSchema
                |If you complete the objective return exactly the JSON response finished by the delimiter %COMPLETED%
                |If you can't complete the tasks do not return the JSON but instead information with the delimiter %FAILED%
            """.trimMargin()
    val deserializationStrategy = serializer<A>()
    return ai(augmentedPrompt, descriptor, deserializationStrategy, jsonSchema)
}

suspend fun <A> ai(
    prompt: String,
    descriptor: SerialDescriptor,
    deserializationStrategy: DeserializationStrategy<A>,
    jsonSchema: JsonObject
): A = resourceScope {
    either {
        val ai = autoAI(prompt).bind()
        val result = ai()
        handleResultAndJson(result, prompt, descriptor, deserializationStrategy, jsonSchema)
    }.getOrElse { throw IllegalStateException(it.joinToString()) }
}

@OptIn(ExperimentalTime::class)
suspend fun ResourceScope.autoAI(
    augmentedPrompt: String
): Either<NonEmptyList<String>, AutoAI> = either {
    val openAIConfig = OpenAIConfig()
    val client = HttpClient(CIO)
    val openAiClient = KtorOpenAIClient(
        engine = client.engine,
        config = openAIConfig,
    )
    val logger = KotlinLogging.logger("Main")
    val embeddings = OpenAIEmbeddings(
        openAIConfig,
        openAiClient,
        logger
    )
    val vectorStore = LocalVectorStore(embeddings)
    val ai = AutoAI(
        LLM("gpt-3.5-turbo"),
        User("AI_Value_Generator"),
        openAiClient,
        Objective(augmentedPrompt),
        vectorStore
    )
    ai
}

private suspend fun <A> handleResultAndJson(
    result: List<Task>,
    prompt: String,
    descriptor: SerialDescriptor,
    deserializationStrategy: DeserializationStrategy<A>,
    jsonSchema: JsonObject
): A {
    val res = result.firstOrNull()?.result
    require(res != null) { "No result found" }
    return try {
        Json.decodeFromString(deserializationStrategy, res)
    } catch (e: Exception) {
        val augmentedPrompt = """
                    |RESULT: 
                    |$res
                    |Exception: 
                    |${e.message}
                    |${e.printStackTrace()}
                    |Objective: $prompt
                    |Instructions: Use the following JSON schema to produce the result on valid json format avoiding the exception
                    |JSON Schema:$jsonSchema
                    |If you complete the objective return exactly the JSON response finished by the delimiter %COMPLETED%
                    |If you can't complete the tasks do not return the JSON but instead information with the delimiter %FAILED%
                    """.trimMargin()
        println("Attempting to Fix JSON due to error: ${e.message}")
        val fixJson = suspend {
            val fixedJson = ai(augmentedPrompt, descriptor, deserializationStrategy, jsonSchema)
            println("Fixed JSON: $fixedJson")
            fixedJson
        }
        fixJson()
    }
}
