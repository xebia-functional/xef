package com.xebia.functional.xef.assistants

import arrow.fx.coroutines.resourceScope
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.RunDelta
import com.xebia.functional.xef.server.assistants.postgres.PostgresAssistant
import com.xebia.functional.xef.server.services.hikariDataSource
import org.jetbrains.exposed.sql.Database

suspend fun main() {

  resourceScope {
    val config = Config(baseUrl = "http://localhost:11434/v1/")
    val chat = OpenAI(config = config, logRequests = true).chat

    val xefDatasource =
      hikariDataSource("jdbc:postgresql://localhost:5433/xef_database", "postgres", "postgres")

    Database.connect(xefDatasource)

    val postgresAssistant = PostgresAssistant(api = chat)
    val assistant = getAssistant(postgresAssistant)
    val assistantInfo = assistant.get()
    println("assistant: $assistantInfo")
    val thread = AssistantThread(api = postgresAssistant)
    println("Enter a message or type 'exit' to quit:")
    while (true) {
      val input = readlnOrNull() ?: break
      if (input == "exit") break
      thread.createMessage(input)
      thread.run(assistant).collect {
        when (it) {
          is RunDelta.MessageDelta ->
            print(it.messageDelta.delta.content.firstOrNull()?.text?.value)
          else -> it.printEvent()
        }
      }
    }
  }
}
