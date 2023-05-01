package com.xebia.functional.auto.agents

import com.xebia.functional.auto.*
import com.xebia.functional.auto.model.Solution
import com.xebia.functional.auto.model.Task
import com.xebia.functional.io.ExecuteCommandOptions
import com.xebia.functional.io.executeCommandAndCaptureOutput
import io.ktor.http.*
import kotlinx.serialization.Serializable
import okio.FileSystem

@Serializable
data class WikipediaAgent(val command: String, val description: String) {

    companion object : Agent<WikipediaAgent> {
        override suspend fun context(task: Task, previousSolutions: List<Solution>): AgentContext<WikipediaAgent> {
            val curlCommand = listOf("curl", "-s", "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&srsearch=${task.objective.encodeURLQueryComponent()}")
            val result = executeCommandAndCaptureOutput(
                curlCommand,
                ExecuteCommandOptions(
                    directory = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString(),
                    abortOnError = true,
                    redirectStderr = true,
                    trim = true
                ))
            val summarized: WikipediaAgent = ai("""
                You are an AI Agent in charge of extracting relevant information for objective:
                ${task.objective}
                Instructions:
                - Extract the relevant information from the following output extracted by command:
                    $curlCommand
                - The output is:
                    ----------------
                    $result
                    ----------------
                - If no relevant information is found, return the following:
                    <No relevant information found>    
            """.trimIndent())
            logger.debug { "WikipediaAgent: $summarized" }
            return AgentContext(task, previousSolutions, summarized, summarized.description)
        }
    }
}
