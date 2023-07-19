@file:JvmName("KotlinPort")

package com.xebia.functional.gpt4all.port

import com.xebia.functional.gpt4all.GPT4All
import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.VectorStore
import kotlinx.coroutines.reactive.asPublisher
import org.reactivestreams.Publisher

@AiDsl
suspend fun promptStreaming(
    gpt4all: GPT4All,
    question: String,
    context: VectorStore,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
): Publisher<String> {
    val promptStreaming = gpt4all.promptStreaming(Prompt(question), context, null, emptyList(), promptConfiguration)

    return promptStreaming.asPublisher()

//    val answer: MutableList<String> = mutableListOf()
//    promptStreaming.asPublisher()
//    promptStreaming.onCompletion {
//        println("\n🤖 Done")
//    }.collect {
//        answer.add(it)
//        print(it)
//    }
//
//    return answer
}
