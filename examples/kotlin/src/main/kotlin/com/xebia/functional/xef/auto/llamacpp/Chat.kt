package com.xebia.functional.xef.auto.llamacpp

import arrow.core.raise.ensure
import arrow.core.raise.recover
import com.xebia.functional.gpt4all.GPT4AllModel
import com.xebia.functional.llamacpp.EmbeddingRequest
import com.xebia.functional.llamacpp.EmbeddingResponse
import com.xebia.functional.llamacpp.Llama
import java.nio.file.Path
import java.util.*

data class ChatError(val content: String)

suspend fun main() {
    recover({
        val resources = "models/llamacpp"
        val path = "$resources/llama-7b.ggmlv3.q8_0.bin"
        val modelType = GPT4AllModel.Type.LLAMA

        val modelPath: Path = Path.of(path)
        ensure(modelPath.toFile().exists()) {
            ChatError("Model at ${modelPath.toAbsolutePath()} cannot be found.")
        }

        Scanner(System.`in`).use { scanner ->
            println("Loading model...")

            Llama(modelPath).use { llama ->
                println("Model loaded!")
                print("Embeddings: ")

                buildList {
                    while (scanner.hasNextLine()) {
                        val prompt: String = scanner.nextLine()
                        if (prompt.equals("exit", ignoreCase = true)) { break }

                        println("...")
                        add(prompt)

                        val request = EmbeddingRequest(listOf(prompt))
                        val response: EmbeddingResponse = llama.createEmbeddings(request)
                        println("Response: ${response.data}")

                        add(response.data)
                        print("Embeddings: ")
                    }
                }

            }
        }
    }) { println(it) }
}
