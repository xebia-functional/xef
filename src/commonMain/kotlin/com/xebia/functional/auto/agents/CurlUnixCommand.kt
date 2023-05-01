package com.xebia.functional.auto.agents

import com.xebia.functional.auto.*
import com.xebia.functional.auto.model.Solution
import com.xebia.functional.auto.model.Task
import kotlinx.serialization.Serializable

@Serializable
data class CurlUnixCommand(val command: String, val description: String) {

    companion object : Agent<CurlUnixCommand> {
        override suspend fun context(task: Task, previousSolutions: List<Solution>): AgentContext<CurlUnixCommand> {
            val unixCommand: CurlUnixCommand = ai(
                """|You are an AI who provides a `curl` command to find information about the following objective by searching on Google:
                   |${task.objective}
               """.trimMargin()
            )
            val output = """|
                | TOOL output, TODO() replace with real call to Unix command
            """.trimMargin()
            return AgentContext(task, previousSolutions, unixCommand, output)
        }
    }
}
