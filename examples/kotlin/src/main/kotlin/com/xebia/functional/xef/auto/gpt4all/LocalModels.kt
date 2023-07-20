package com.xebia.functional.xef.auto.gpt4all

import com.xebia.functional.gpt4all.GPT4All
import com.xebia.functional.gpt4all.GPT4AllPromptConfiguration
import com.xebia.functional.gpt4all.Gpt4AllModel
import com.xebia.functional.gpt4all.getOrThrow
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.auto.ai
import java.nio.file.Path


suspend fun main() {
    val userDir = "${System.getProperty("user.dir")}/models/gpt4all/"

    println("🤖 Supported models:")
    val filteredModels = Gpt4AllModel.supportedModels.filter { it.url != null }
    filteredModels.forEachIndexed { index, it ->
        println("🤖 $index: ${it.name} ${it.url?.let { "- $it" }}")
    }

    print("\n🤖 Select a model: ")
    val selectedModel = readlnOrNull()?.toIntOrNull() ?: 0
    val model = filteredModels[selectedModel]
    val url = model.url ?: error("🤖 No url for model $model")
    val modelPath: Path = Path.of("${userDir}/${model.filename}")
    val gpt4All = GPT4All(url, modelPath)
    println("🤖 GPT4All loaded: $gpt4All")

//    val url = "https://gpt4all.io/models/ggml-gpt4all-j-v1.3-groovy.bin"

    print("\n🤖 Enter the prompt: ")
    val userInput = readlnOrNull() ?: return

//    val prompt = "###User:\n$userInput\n###Assistant:"

//    ai(model = gpt4All) { //this: GPT4AllAIScope
//        promptConfiguration(a)
//        promptMessage("What is the meaning of life?")
//
//        promptConfiguration(b)
//        promptMessage("How many stars are in the sky?")
//    }

    ai {
        gpt4All.use { gptModel: GPT4All ->
            val promptConfiguration: PromptConfiguration = GPT4AllPromptConfiguration()
            val res =
                gptModel.promptMessage(userInput, context = this.context, promptConfiguration = promptConfiguration)
            println("\n🤖 Done")
            println(res)
        }

        // Alternative
//        val res = promptMessage(
//            prompt = userInput,
//            model = gpt4All,
//            promptConfiguration = PromptConfiguration {
//                numberOfPredictions(4096)
//            }
//        )
//        println(res)


    }.getOrThrow()
}
