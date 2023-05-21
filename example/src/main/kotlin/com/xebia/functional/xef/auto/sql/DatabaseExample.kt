package com.xebia.functional.xef.auto.sql

import arrow.core.raise.catch
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.getOrThrow
import com.xebia.functional.xef.auto.promptMessage
import com.xebia.functional.xef.sql.SQL
import com.xebia.functional.xef.sql.jdbc.DatabaseConfig

val config = DatabaseConfig(
  vendor = System.getenv("XEF_SQL_DB_VENDOR") ?: "mysql",
  host = System.getenv("XEF_SQL_DB_HOST") ?: "localhost",
  username = System.getenv("XEF_SQL_DB_USER") ?: "user",
  password = System.getenv("XEF_SQL_DB_PASSWORD") ?: "password",
  port = System.getenv("XEF_SQL_DB_PORT")?.toInt() ?: 3306,
  database = System.getenv("XEF_SQL_DB_DATABASE") ?: "database",
  llmModelType = ModelType.GPT_3_5_TURBO
)

suspend fun main() = ai {
  SQL.fromJdbcConfig(config) {
    println("llmdb> Welcome to the LLMDB (An LLM interface to your SQL Database) !")
    println("llmdb> You can ask me questions about the database and I will try to answer them.")
    println("llmdb> You can type `exit` to exit the program.")
    println("llmdb> Loading recommended prompts...")
    val interestingPrompts = getInterestingPromptsForDatabase()
    interestingPrompts.forEach {
      println("llmdb> ${it}")
    }
    while (true) {
      // a cli chat with the content
      print("user> ")
      val input = readln()
      if (input == "exit") break
      catch({
        extendContext(*promptQuery(input).toTypedArray())
        val result = promptMessage("""|
          |You are a database assistant that helps users to query and summarize results from the database.
          |Instructions:
          |1. Summarize the information provided in the `Context` and follow to step 2.
          |2. If the information relates to the `input` then answer the question otherwise return just the summary.
          |```input
          |$input
          |```
          |3. Try to answer and provide information with as much detail as you can
        """.trimMargin(), bringFromContext = 200)
        result.forEach {
          println("llmdb> ${it}")
        }
      }, { exception ->
        println("llmdb> ${exception.message}")
        exception.printStackTrace()
      })
    }
  }
}.getOrThrow()

