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
import java.util.Scanner

suspend fun main(args: Array<String>) {
    ai {
        val modelName = "llama"
        val modelPath = "examples/kotlin/src/main/kotlin/com/xebia/functional/xef/auto/gpt4all/models/ggml-gpt4all-l13b-snoozy.bin"

        val type: GPT4AllModel.Type =
            when (modelName) {
                "llama" -> GPT4AllModel.Type.LLAMA
                "gptj" -> GPT4AllModel.Type.GPTJ
                else -> raise(AIError.ChatError("Model type $modelName is not recognized."))
            }

        val path: Path = Path.of(modelPath)
        ensure(path.toFile().exists()) {
            AIError.ChatError("Model at ${path.toAbsolutePath()} cannot be found.")
        }

        println("Loading...")
        System.out.flush()

        println("Loading...")
        System.out.flush()
        Scanner(System.`in`).use { scanner ->
            GPT4All(path, type).use { gpt4All ->
                println()
                print("Prompt[0]: ")
                System.out.flush()

                buildList {
                    while (scanner.hasNextLine()) {
                        val prompt: String = scanner.nextLine()
                        if (prompt.equals("exit", ignoreCase = true)) {
                            break
                        }
                        println("Thinking...")
                        System.out.flush()

                        val promptMessage = Message(Message.Role.USER, prompt)
                        add(promptMessage)
                        val response: Response = gpt4All.chatCompletion(this)
                        println("Response: " + response.choices[0].content)
                        add(response.choices[0])
                        println()
                        print("Prompt[" + size / 2 + "]: ")
                        System.out.flush()
                    }
                }
            }
        }
    }.getOrThrow()
}
