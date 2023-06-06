package com.xebia.functional.xef.auto.gpt4all

import arrow.core.raise.ensure
import com.xebia.functional.gpt4all.GPT4All
import com.xebia.functional.gpt4all.GPT4AllModel
import com.xebia.functional.gpt4all.Message
import com.xebia.functional.gpt4all.Response
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.getOrThrow
import java.nio.file.Path
import java.util.*

suspend fun main() {
    ai {
        val resources = "models/gpt4all"
        val path = "$resources/ggml-gpt4all-j-v1.3-groovy.bin"
        val modelType = GPT4AllModel.Type.GPTJ

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

                        val response: Response = gpt4All.chatCompletion(this)
                        println("Response: ${response.choices[0].content}")

                        add(response.choices[0])
                        print("Prompt: ")
                    }
                }
            }
        }
    }.getOrThrow()
}
