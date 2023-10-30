package com.xebia.functional.xef.server.db.local

import com.xebia.functional.xef.server.db.VectorStoreConfig
import com.xebia.functional.xef.server.services.LocalVectorStoreService
import kotlinx.serialization.Serializable
import org.slf4j.Logger

@Serializable
class LocalVectorStoreConfig() : VectorStoreConfig {
  override suspend fun getVectorStoreService(logger: Logger): LocalVectorStoreService =
    LocalVectorStoreService()

  companion object {
    fun load(): LocalVectorStoreConfig = LocalVectorStoreConfig()
  }
}
