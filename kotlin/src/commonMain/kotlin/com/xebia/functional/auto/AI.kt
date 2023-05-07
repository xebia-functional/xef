package com.xebia.functional.auto

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.recover
import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.auto.serialization.buildJsonSchema
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.KtorOpenAIClient
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.tools.Tool
import com.xebia.functional.vectorstores.LocalVectorStore
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.serializer
import kotlin.experimental.ExperimentalTypeInference
import kotlin.time.ExperimentalTime

@DslMarker
annotation class DSL


@DSL
@OptIn(ExperimentalTypeInference::class)
suspend inline fun <reified A> ai(
    @BuilderInference noinline block: suspend AI.() -> A
): Either<AIError, A> =
    resourceScope {
        either {
            try {
                AI.run { default() }.bind().block()
            } catch (e: AI.AIInternalException) {
                raise(e.error)
            }
        }
    }

abstract class AI {

    @PublishedApi
    internal class AIInternalException(val error: AIError) : CancellationException(error.toString())

    abstract val config: Config

    abstract val agents: Atomic<List<Agent>>

    abstract suspend operator fun <A> Raise<AIError>.invoke(
        prompt: String,
        serializationConfig: SerializationConfig<A>,
        maxAttempts: Int = 5,
    ): A

    suspend inline fun <A> agent(vararg tool: Tool, scope: () -> A): A {
        val scopedAgent = Agent(*tool)
        agents.update { it + scopedAgent }
        val result = scope()
        agents.update { it - scopedAgent }
        return result
    }

    @DSL
    suspend inline fun <reified A> ai(prompt: String): A =
        either { invoke<A>(prompt) }.getOrElse { throw AIInternalException(it) }

    @PublishedApi
    internal suspend inline operator fun <reified A> Raise<AIError>.invoke(prompt: String): A {
        val serializer = serializer<A>()
        val serializationConfig: SerializationConfig<A> = SerializationConfig(
            jsonSchema = buildJsonSchema(serializer.descriptor, false),
            descriptor = serializer.descriptor,
            deserializationStrategy = serializer
        )
        return invoke(prompt, serializationConfig)
    }

    companion object {

        fun ResourceScope.fromConfig(config: Config): AI =
            DefaultAI(config)

        @OptIn(ExperimentalTime::class)
        suspend fun ResourceScope.default(): Either<AIError, AI> =
            either {
                val openAIConfig = recover({
                    OpenAIConfig()
                }) { raise(AIError(it.joinToString(", "))) }
                val openAiClient: OpenAIClient = KtorOpenAIClient(openAIConfig)
                val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
                val vectorStore = LocalVectorStore(embeddings)
                fromConfig(
                    Config(
                        engine = null,
                        openAiClient,
                        vectorStore,
                    )
                )
            }
    }

}
