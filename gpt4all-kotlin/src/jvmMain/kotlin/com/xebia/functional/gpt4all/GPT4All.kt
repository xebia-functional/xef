package com.xebia.functional.gpt4all

import com.sun.jna.platform.unix.LibCAPI
import com.xebia.functional.gpt4all.libraries.LLModelContext
import java.nio.file.Path

interface GPT4All : AutoCloseable {
    val gpt4allModel: GPT4AllModel

    suspend fun createCompletion(prompt: String): CompletionResponse

    suspend fun createChatCompletion(messages: List<Message>): ChatCompletionResponse

    companion object {
        operator fun invoke(
            path: Path,
            modelType: LLModel.Type,
            generationConfig: GenerationConfig = GenerationConfig()
        ): GPT4All = object : GPT4All {
            override val gpt4allModel: GPT4AllModel = GPT4AllModel(path, modelType)

            override suspend fun createCompletion(prompt: String): CompletionResponse {
                val response: String = generateCompletion(prompt, generationConfig)
                val name: String = gpt4allModel.llModel.name
                return CompletionResponse(
                    name,
                    prompt.length,
                    response.length,
                    totalTokens = prompt.length + response.length,
                    listOf(Completion(response))
                )
            }

            override suspend fun createChatCompletion(messages: List<Message>): ChatCompletionResponse {
                val prompt: String = messages.buildPrompt()
                val response: String = generateCompletion(prompt, generationConfig)
                val name: String = gpt4allModel.llModel.name
                return ChatCompletionResponse(
                    name,
                    prompt.length,
                    response.length,
                    totalTokens = prompt.length + response.length,
                    listOf(Message(Message.Role.ASSISTANT, response))
                )
            }

            override fun close(): Unit = gpt4allModel.close()

            private fun List<Message>.buildPrompt(): String {
                val messages: String = joinToString("") { message ->
                    when (message.role) {
                        Message.Role.SYSTEM -> message.content
                        Message.Role.USER -> "\n### Human: ${message.content}"
                        Message.Role.ASSISTANT -> "\n### Response: ${message.content}"
                    }
                }
                return "$messages\n### Response:"
            }

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

                return gpt4allModel.prompt(prompt, context)
            }
        }
    }
}
