package com.xebia.functional.auto

import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.parMapOrAccumulate
import com.xebia.functional.auto.serialization.sample
import com.xebia.functional.chains.VectorQAChain
import com.xebia.functional.llm.openai.ChatCompletionRequest
import com.xebia.functional.llm.openai.ChatCompletionResponse
import com.xebia.functional.llm.openai.Message
import com.xebia.functional.llm.openai.Role

fun ResourceScope.DefaultAI(config: Config): AI = object : AI() {

    override val config: Config = config

    override val agents: Atomic<List<Agent>> = Atomic.unsafe(emptyList())

    private fun logTruncated(ctx: String, msg: String, max: Int = 100): Unit =
        logger.debug {
            if (msg.length > max) {
                "[$ctx] ${msg.take(max)}..."
            } else {
                "[$ctx] $msg"
            }
        }

    override suspend operator fun <A> Raise<AIError>.invoke(
        prompt: String, serializationConfig: SerializationConfig<A>,
        maxAttempts: Int
    ): A {
        logTruncated("AI", "Solving objective: $prompt", 100)
        val result = openAIChatCall(prompt, prompt, serializationConfig)
        logTruncated("AI", "Response: $result", 100)
        return catch({
            json.decodeFromString(serializationConfig.deserializationStrategy, result)
        }) { e ->
            if (maxAttempts <= 0) raise(AIError(result))
            else {
                logTruncated("System", "Error deserializing result, trying again... ${e.message}", 100)
                invoke(prompt, serializationConfig, maxAttempts - 1).also { logger.debug { "Fixed JSON: $it" } }
            }
        }
    }

    private suspend fun Raise<AIError>.openAIChatCall(
        question: String,
        promptWithContext: String,
        serializationConfig: SerializationConfig<*>,

    ): String {
        //run the agents so they store context in the database
        agents.get().forEach { agent ->
            agent.storeResults(config.vectorStore)
        }
        //run the vectorQAChain to get the answer
        val numOfDocs = 10
        val outputVariable = "answer"
        val chain = VectorQAChain(
            config.openAIClient,
            config.vectorStore,
            numOfDocs,
            outputVariable
        )
        val contextQuestion = """|
            |Provide a solution as answer to the question or objective below:
            |```
            |$question
            |```
        """.trimMargin()
        val chainResults: Map<String, String> = chain.run(contextQuestion).getOrElse { raise(AIError(it.reason)) }
        logger.debug { "Chain results: $chainResults" }
        val promptWithMemory =
            if (chainResults.isNotEmpty())
                """
                |Instructions: Use the [Information] below delimited by 3 backticks to accomplish
                |the [Objective] at the end of the prompt.
                |Try to match the data returned in the [Objective] with this [Information] as best as you can.
                |[Information]:
                |```
                |${chainResults.entries.joinToString("\n") { (k, v) -> "$k: $v" }}
                |```
                |$promptWithContext
                """.trimMargin()
            else promptWithContext
        val augmentedPrompt = """
                |$promptWithMemory
                |
                |Response Instructions: Use the following JSON schema to produce the result exclusively in valid JSON format
                |JSON Schema:
                |${serializationConfig.jsonSchema}
                |Response Example:
                |${serializationConfig.descriptor.sample()}
                |Response:
            """.trimMargin()
        val res = chatCompletionResponse(augmentedPrompt, "gpt-3.5-turbo", "AI_Value_Generator")
        val msg = res.choices.firstOrNull()?.message?.content
        requireNotNull(msg) { "No message found in result: $res" }
        logTruncated("AI", "Response: $msg", 100)
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

