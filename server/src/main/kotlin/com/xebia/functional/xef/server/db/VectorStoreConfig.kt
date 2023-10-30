package com.xebia.functional.xef.server.db

import com.xebia.functional.xef.server.services.VectorStoreService
import org.slf4j.Logger

interface VectorStoreConfig {
  suspend fun getVectorStoreService(logger: Logger): VectorStoreService
}
