package com.xebia.functional.gpt4all

import com.sun.jna.platform.unix.LibCAPI
import com.xebia.functional.gpt4all.libraries.LLModelContext
import java.nio.file.Path

interface GPT4All : AutoCloseable {
    suspend fun createCompletion(request: CompletionRequest): CompletionResponse
    suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse
    suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResponse

    companion object {
        operator fun invoke(
            path: Path,
            modelType: LLModel.Type
        ): GPT4All = object : GPT4All {
            val gpt4allModel: GPT4AllModel = GPT4AllModel(path, modelType)

            override suspend fun createCompletion(request: CompletionRequest): CompletionResponse =
                with(request) {
                    val response: String = generateCompletion(prompt, generationConfig)
                    return CompletionResponse(
                        gpt4allModel.llModel.name,
                        prompt.length,
                        response.length,
                        totalTokens = prompt.length + response.length,
                        listOf(Completion(response))
                    )
                }

            override suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse =
                with(request) {
                    val prompt: String = messages.buildPrompt()
                    val response: String = generateCompletion(prompt, generationConfig)
                    return ChatCompletionResponse(
                        gpt4allModel.llModel.name,
                        prompt.length,
                        response.length,
                        totalTokens = prompt.length + response.length,
                        listOf(Message(com.xebia.functional.gpt4all.Message.Role.ASSISTANT, response))
                    )
                }

            override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResponse {
                TODO("Not yet implemented")
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
