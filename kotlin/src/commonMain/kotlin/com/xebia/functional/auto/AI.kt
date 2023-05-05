package com.xebia.functional.auto

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.recover
import arrow.fx.coroutines.resourceScope
import arrow.optics.optics
import com.xebia.functional.auto.serialization.buildJsonSchema
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.KtorOpenAIClient
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.vectorstores.LocalVectorStore
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.serializer
import kotlin.experimental.ExperimentalTypeInference
import kotlin.time.ExperimentalTime

@DslMarker
annotation class DSL

@optics
data class Tool(
    val name: String,
    val description: String,
    val action: suspend (prompt: String) -> String,
)

interface Agent {
    suspend fun tool(tool: Tool): Unit
}

@DSL
@OptIn(ExperimentalTypeInference::class)
suspend inline fun <reified A> ai(ai: AI? = null, @BuilderInference noinline prompt: suspend AI.() -> A): Either<AIError, A> =
    AI(ai, prompt)

@DSL
suspend inline fun <reified A> AI.ai(prompt: String): A =
    AI<A>(this, prompt).getOrElse { raise(it) }

interface AI : Raise<AIError> {
    val config: Config

    suspend operator fun <A> invoke(prompt: String, serializationConfig: SerializationConfig<A>): A

    companion object {

        @OptIn(ExperimentalTypeInference::class)
        suspend inline operator fun <reified A> invoke(ai: AI? = null, @BuilderInference noinline prompt: suspend AI.() -> A): Either<AIError, A> =
            either {
                val selected = ai ?: default().bind()
                selected.run { prompt() }
            }

        suspend inline operator fun <reified A> invoke(ai: AI? = null, prompt: String): Either<AIError, A> =
            either {
                val selectedAI: AI = ai ?: default().bind()
                val serializer = serializer<A>()
                val serializationConfig: SerializationConfig<A> = SerializationConfig(
                    jsonSchema = buildJsonSchema(serializer.descriptor, false),
                    descriptor = serializer.descriptor,
                    deserializationStrategy = serializer
                )
                selectedAI.run { invoke(prompt, serializationConfig) }
            }

        suspend fun fromConfig(config: Config): Either<AIError, AI> = resourceScope {
            either {
                DefaultAI(config, this)
            }
        }

        @OptIn(ExperimentalTime::class)
        suspend fun default(): Either<AIError, AI> = resourceScope {
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
                ).bind()
            }
        }
    }

}
