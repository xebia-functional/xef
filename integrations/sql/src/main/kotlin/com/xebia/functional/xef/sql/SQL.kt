package com.xebia.functional.xef.sql

import com.xebia.functional.xef.auto.AIScope
import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.sql.jdbc.DatabaseConfig
import com.xebia.functional.xef.sql.jdbc.JDBCSQLImpl

interface SQL {

  companion object {
    suspend fun <A> fromJdbcConfig(config: DatabaseConfig, block: suspend JDBCSQLImpl.() -> A): A =
      JDBCSQLImpl(config).use {
        block(it)
      }
  }

  /**
   * Generates SQL from the DDL and input prompt
   */
  @AiDsl
  suspend fun AIScope.sql(ddl: String, input: String): List<String>

  /**
   * Chooses a subset of tables from the list of [tableNames] based on the [prompt]
   */
  @AiDsl
  suspend fun AIScope.selectTablesForPrompt(tableNames: String, prompt: String): List<String>

  /**
   * Returns a list of documents found in the database for the given [prompt]
   */
  @AiDsl
  suspend fun AIScope.promptQuery(prompt: String): List<String>

  /**
   * Returns a recommendation of prompts that are interesting for the database
   * based on the internal ddl schema
   */
  @AiDsl
  suspend fun AIScope.getInterestingPromptsForDatabase(): List<String>

}
