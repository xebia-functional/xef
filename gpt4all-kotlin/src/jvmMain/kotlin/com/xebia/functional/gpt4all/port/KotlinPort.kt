@file:JvmName("KotlinPort")

package com.xebia.functional.gpt4all.port

import com.xebia.functional.gpt4all.GPT4All
import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.VectorStore
import kotlinx.coroutines.flow.onCompletion

@AiDsl
suspend fun promptStreaming(
    gpt4all: GPT4All,
    question: String,
    context: VectorStore,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
): List<String> {
    val promptStreaming = gpt4all.promptStreaming(Prompt(question), context, null, emptyList(), promptConfiguration)

    val answer: MutableList<String> = mutableListOf()
    promptStreaming.onCompletion {
        println("\n🤖 Done")
    }.collect {
        answer.add(it)
        print(it)
    }

    return answer
}
