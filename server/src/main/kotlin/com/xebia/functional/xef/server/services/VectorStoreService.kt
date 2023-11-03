package com.xebia.functional.xef.server.services

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.xebia.functional.xef.server.db.VectorStoreConfig
import com.xebia.functional.xef.server.db.local.LocalVectorStoreConfig
import com.xebia.functional.xef.server.db.psql.PSQLVectorStoreConfig
import com.xebia.functional.xef.store.VectorStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon

enum class XefVectorStoreType {
  PSQL,
  LOCAL
}

enum class Provider {
  OPENAI
}

abstract class VectorStoreService {
  abstract fun getVectorStore(
    provider: Provider = Provider.OPENAI,
    token: String? = null
  ): VectorStore

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun load(configNamespace: String, config: Config?): VectorStoreConfig =
      withContext(Dispatchers.IO) {
        val rawConfig = config ?: ConfigFactory.load().resolve()
        val jdbcConfig = rawConfig.getConfig(configNamespace)
        val typeConfig = Hocon.decodeFromConfig(VectorStoreTypeConfig.serializer(), jdbcConfig)
        when (typeConfig.type) {
          XefVectorStoreType.PSQL -> PSQLVectorStoreConfig.load(configNamespace, rawConfig)
          XefVectorStoreType.LOCAL -> LocalVectorStoreConfig.load()
        }
      }
  }
}

@Serializable
class VectorStoreTypeConfig(
  val type: XefVectorStoreType,
)
