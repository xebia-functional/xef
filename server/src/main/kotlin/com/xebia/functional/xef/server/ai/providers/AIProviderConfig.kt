package com.xebia.functional.xef.server.ai.providers

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon

@Serializable
enum class AIProvider {
    OpenAI,
    MLflow
}

@Serializable
class AIProviderConfig(
    val aiProvider: AIProvider,
    val baseUri: String
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        suspend fun load(
            configNamespace: String,
            config: Config? = null
        ): AIProviderConfig =
            withContext(Dispatchers.IO) {
                val rawConfig = config ?: ConfigFactory.load().resolve()
                val jdbcConfig = rawConfig.getConfig(configNamespace)
                Hocon.decodeFromConfig(serializer(), jdbcConfig)
            }

    }
}