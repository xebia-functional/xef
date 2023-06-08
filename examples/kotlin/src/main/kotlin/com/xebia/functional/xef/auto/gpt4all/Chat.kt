package com.xebia.functional.xef.auto.gpt4all

import arrow.core.raise.either
import arrow.core.raise.ensure
import com.xebia.functional.gpt4all.*
import com.xebia.functional.xef.AIError
import java.nio.file.Path
import java.util.*

suspend fun main() {
    either {
        val resources = "models/gpt4all"
        val path = "$resources/ggml-gpt4all-j-v1.3-groovy.bin"
        val modelType = LLModel.Type.GPTJ

        val modelPath: Path = Path.of(path)
        ensure(modelPath.toFile().exists()) {
            AIError.ChatError("Model at ${modelPath.toAbsolutePath()} cannot be found.")
        }

        Scanner(System.`in`).use { scanner ->
            println("Loading model...")

            GPT4All(modelPath, modelType).use { gpt4All ->
                println("Model loaded!")
                print("Prompt: ")

                buildList {
                    while (scanner.hasNextLine()) {
                        val prompt: String = scanner.nextLine()
                        if (prompt.equals("exit", ignoreCase = true)) { break }

                        println("...")
                        val promptMessage = Message(Message.Role.USER, prompt)
                        add(promptMessage)

                        val request = ChatCompletionRequest(this, GenerationConfig())
                        val response: ChatCompletionResponse = gpt4All.createChatCompletion(request)
                        println("Response: ${response.choices[0].content}")

                        add(response.choices[0])
                        print("Prompt: ")
                    }
                }
            }
        }
    }
}
