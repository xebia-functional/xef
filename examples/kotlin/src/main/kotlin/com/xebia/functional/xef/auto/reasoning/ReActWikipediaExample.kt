package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.xef.auto.conversation
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.getOrThrow
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.reasoning.search.SearchWikipedia
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.ReActAgent

suspend fun main() {
    conversation {
        val model = OpenAI.DEFAULT_CHAT
        val serialization = OpenAI.DEFAULT_SERIALIZATION
        val math = LLMTool.create(
            name = "Calculator",
            description = "Perform math operations and calculations processing them with an LLM model. The tool input is a simple string containing the operation to solve expressed in numbers and math symbols.",
            model = model,
            scope = this
        )
        val search = SearchWikipedia(model = model, scope = this)

        val reActAgent = ReActAgent(
            model = serialization,
            scope = this,
            tools = listOf(
                search,
                math,
            ),
        )
        val result =
            reActAgent.run(
                listOf(
                    Message.userMessage {
                        "Find and multiply the number of human bones by the number of Metallica albums"
                    })
            )
        println(result)
    }.getOrThrow()
}
