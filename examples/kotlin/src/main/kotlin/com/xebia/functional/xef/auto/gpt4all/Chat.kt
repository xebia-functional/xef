package com.xebia.functional.xef.auto.gpt4all

import arrow.core.raise.ensure
import arrow.core.raise.recover
import com.xebia.functional.gpt4all.GPT4All
import com.xebia.functional.gpt4all.LLModel
import com.xebia.functional.gpt4all.getOrThrow
import com.xebia.functional.xef.auto.ai
import java.nio.file.Path

data class ChatError(val content: String)

suspend fun main() {
    recover({
        val resources = "models/gpt4all"
        val path = "$resources/ggml-gpt4all-j-v1.3-groovy.bin"
        val modelType = LLModel.Type.GPTJ

        val modelPath: Path = Path.of(path)
        ensure(modelPath.toFile().exists()) {
            ChatError("Model at ${modelPath.toAbsolutePath()} cannot be found.")
        }
        val gpT4All = GPT4All(modelPath, modelType)

        ai {
            val hello = gpT4All.promptMessage("Say hello!")
            println(hello)
        }.getOrThrow()
    }) { println(it) }
}
