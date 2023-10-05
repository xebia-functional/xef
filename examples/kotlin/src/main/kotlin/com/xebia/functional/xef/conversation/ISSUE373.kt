package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.promptMessage
import com.xebia.functional.xef.prompt.Prompt
import java.io.File

suspend fun main() {
    OpenAI.conversation() {
        val filePath = "examples/kotlin/src/main/resources/huberman.txt"
        val file = File(filePath)

        addContext(file.readText())

        val summary = promptMessage(Prompt("create a summary"))
        println(summary)
    }
}