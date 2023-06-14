package com.xebia.functional.gpt4all

import com.sun.jna.platform.unix.LibCAPI
import com.xebia.functional.gpt4all.libraries.LLModelContextParams
import java.nio.file.Path

interface GPT4All : AutoCloseable {
    suspend fun createCompletion(request: CompletionRequest): CompletionResponse
    suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse

    companion object {
        operator fun invoke(
            path: Path,
            modelType: GPT4AllModel.Type
        ): GPT4All = object : GPT4All {
            val gpt4allModel: GPT4AllModel =
                when(modelType) {
                    GPT4AllModel.Type.LLAMA -> GPT4AllModel.LLAMA(path)
                    GPT4AllModel.Type.GPTJ -> GPT4AllModel.GPTJ(path)
                    GPT4AllModel.Type.MPT -> GPT4AllModel.MPT(path)
            }

            override suspend fun createCompletion(request: CompletionRequest): CompletionResponse =
                with(request) {
                    val response: String = generateCompletion(prompt, generationConfig)
                    return CompletionResponse(
                        gpt4allModel.name,
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
                        gpt4allModel.name,
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
                config: GenerationConfig
            ): String {
                val contextParams: LLModelContextParams = config.toLLModelContextParams()
                return gpt4allModel.prompt(prompt, contextParams)
            }
        }
    }
}

private fun GenerationConfig.toLLModelContextParams(): LLModelContextParams =
    LLModelContextParams(
        logits_size = LibCAPI.size_t(logitsSize.toLong()),
        tokens_size = LibCAPI.size_t(tokensSize.toLong()),
        n_past = nPast,
        n_ctx = nCtx,
        n_predict = nPredict,
        top_k = topK,
        top_p = topP.toFloat(),
        temp = temp.toFloat(),
        n_batch = nBatch,
        repeat_penalty = repeatPenalty.toFloat(),
        repeat_last_n = repeatLastN,
        context_erase = contextErase.toFloat()
    )
