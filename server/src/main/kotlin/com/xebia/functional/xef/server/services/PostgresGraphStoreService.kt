package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.store.GraphStore
import com.xebia.functional.xef.store.PGGraphStore
import javax.sql.DataSource
import org.slf4j.Logger

class PostgresGraphStoreService(
  private val logger: Logger,
  private val dataSource: DataSource,
) : GraphStoreService() {

  override fun getGraphStore(): GraphStore {
    return PGGraphStore(
      dataSource = dataSource,
    )
  }
}
