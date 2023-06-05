package com.xebia.functional.gpt4all

import com.sun.jna.platform.unix.LibCAPI
import java.nio.file.Path

data class Message(val role: Role, val content: String) {
    enum class Role {
        SYSTEM,
        USER,
        ASSISTANT
    }
}

data class Response(
    val modelName: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val choices: List<Message>
)

data class GenerationConfig(
    val logitsSize: Int = 0,
    val tokensSize: Int = 0,
    val nPast: Int = 0,
    val nCtx: Int = 1024,
    val nPredict: Int = 128,
    val topK: Int = 40,
    val topP: Double = 0.9,
    val temp: Double = 0.1,
    val nBatch: Int = 8,
    val repeatPenalty: Double = 1.2,
    val repeatLastN: Int = 10,
    val contextErase: Double = 0.5
)

interface GPT4All : AutoCloseable {
    val gpt4AllModel: GPT4AllModel

    suspend fun chatCompletion(
        messages: List<Message>,
        verbose: Boolean = false
    ): Response

    companion object {
        operator fun invoke(
            path: Path,
            modelType: GPT4AllModel.Type
        ): GPT4All = object : GPT4All {
            override val gpt4AllModel: GPT4AllModel =
                when (modelType) {
                    GPT4AllModel.Type.LLAMA -> LlamaModel(path).also { it.loadModel() }
                    GPT4AllModel.Type.GPTJ -> GPTJModel(path).also { it.loadModel() }
                }

            override suspend fun chatCompletion(
                messages: List<Message>,
                verbose: Boolean
            ): Response {
                val prompt: String = messages.buildPrompt()
                if (verbose) {
                    println(prompt)
                }

                val response: String = generateCompletion(prompt, GenerationConfig())
                if (verbose) {
                    println(response)
                }

                return Response(
                    gpt4AllModel.name,
                    prompt.length,
                    response.length,
                    totalTokens = prompt.length + response.length,
                    listOf(Message(Message.Role.ASSISTANT, response))
                )
            }

            override fun close(): Unit = gpt4AllModel.close()

            private fun List<Message>.buildPrompt(): String =
                map { message ->
                    when (message.role) {
                        Message.Role.SYSTEM -> message.content
                        Message.Role.USER -> "\n ${message.content}"
                        Message.Role.ASSISTANT -> "\n### Response: ${message.content}"
                    }
                }.toString()

            private fun generateCompletion(
                prompt: String,
                generationConfig: GenerationConfig
            ): String {
                val context = LLModelContext(
                    logits_size = LibCAPI.size_t(generationConfig.logitsSize.toLong()),
                    tokens_size = LibCAPI.size_t(generationConfig.tokensSize.toLong()),
                    n_past = generationConfig.nPast,
                    n_ctx = generationConfig.nCtx,
                    n_predict = generationConfig.nPredict,
                    top_k = generationConfig.topK,
                    top_p = generationConfig.topP.toFloat(),
                    temp = generationConfig.temp.toFloat(),
                    n_batch = generationConfig.nBatch,
                    repeat_penalty = generationConfig.repeatPenalty.toFloat(),
                    repeat_last_n = generationConfig.repeatLastN,
                    context_erase = generationConfig.contextErase.toFloat()
                )

                val responseBuffer = StringBuffer()

                with(gpt4AllModel) {
                    adapter.llmodel_prompt(
                        model,
                        prompt,
                        { _, _ -> true },
                        { _, response -> responseBuffer.append(response).let { true } },
                        { _ -> true },
                        context
                    )
                }
                return responseBuffer.toString()
            }

        }
    }
}
