package com.xebia.functional.auto

import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.KtorOpenAIClient
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.vectorstores.LocalVectorStore
import com.xebia.functional.vectorstores.VectorStore
import io.ktor.client.engine.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.JsonObject
import kotlin.time.ExperimentalTime

data class Config(
    val engine: HttpClientEngine? = null,
    val openAIClient: OpenAIClient,
    val vectorStore: VectorStore,
) {
    companion object {
        @OptIn(ExperimentalTime::class)
        suspend fun default(
            engine: HttpClientEngine? = null,
        ): Config = resourceScope {
            either {
                val openAIConfig = OpenAIConfig()
                val openAiClient = KtorOpenAIClient(openAIConfig, engine)
                val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
                val vectorStore = LocalVectorStore(embeddings)
                Config(
                    engine,
                    openAiClient,
                    vectorStore,
                )
            }.getOrElse { throw IllegalStateException(it.joinToString()) }
        }
    }
}

data class SerializationConfig<A>(
    val jsonSchema: JsonObject,
    val descriptor: SerialDescriptor,
    val deserializationStrategy: DeserializationStrategy<A>,
)
