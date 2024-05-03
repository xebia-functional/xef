package com.xebia.functional.xef.server.assistants.postgres

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.xef.llm.assistants.local.GeneralAssistants
import com.xebia.functional.xef.server.assistants.postgres.tables.*

object PostgresAssistant {
  operator fun invoke(api: Chat): Assistants =
    GeneralAssistants(
      api = api,
      assistantPersistence = AssistantsTable,
      assistantFilesPersistence = AssistantsFilesTable,
      threadPersistence = ThreadsTable,
      messagePersistence = MessagesTable,
      messageFilesPersistence = MessagesFilesTable,
      runPersistence = RunsTable,
      runStepPersistence = RunsStepsTable
    )
}
