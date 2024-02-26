package com.xebia.functional.xef.server.services

import arrow.fx.coroutines.Atomic
import com.xebia.functional.xef.store.GraphStore
import com.xebia.functional.xef.store.PGGraphStore
import com.xebia.functional.xef.store.config.PostgreSQLGraphStoreConfig
import javax.sql.DataSource
import org.slf4j.Logger

class PostgresGraphStoreService(
  private val logger: Logger,
  private val dataSource: DataSource,
  private val config: PostgreSQLGraphStoreConfig
) : GraphStoreService() {

  private val graphStores: Atomic<MutableMap<String, GraphStore>> = Atomic.unsafe(mutableMapOf())

  override suspend fun getGraphStore(graphId: String): GraphStore {
    return graphStores.get().getOrPut(graphId) {
      PGGraphStore(graphId, config, dataSource, logger).also { it.init() }
    }
  }
}
