package com.xebia.functional.gpt4all

import com.sun.jna.platform.unix.LibCAPI
import com.xebia.functional.gpt4all.libraries.LLModelContext
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import java.nio.file.Path
import java.util.*
import kotlin.io.path.name

interface GPT4All : AutoCloseable, Chat, Completion {

    val gpt4allModel : GPT4AllModel

    override fun close() {
    }

    companion object {
        operator fun invoke(
            path: Path,
            modelType: LLModel.Type,
            generationConfig: GenerationConfig = GenerationConfig(),
        ): GPT4All = object : GPT4All {

            override val gpt4allModel = GPT4AllModel.invoke(path, modelType)

            override suspend fun createCompletion(request: CompletionRequest): CompletionResult =
                with(request) {
                    val response: String = generateCompletion(prompt, generationConfig)
                    return CompletionResult(
                        UUID.randomUUID().toString(),
                        path.name,
                        System.currentTimeMillis(),
                        path.name,
                        listOf(CompletionChoice(response, 0, null, null)),
                        Usage.ZERO
                    )
                }

            override suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse =
                with(request) {
                    val prompt: String = messages.buildPrompt()
                    val response: String = generateCompletion(prompt, generationConfig)
                    return ChatCompletionResponse(
                        UUID.randomUUID().toString(),
                        path.name,
                        System.currentTimeMillis().toInt(),
                        path.name,
                        Usage.ZERO,
                        listOf(Choice(Message(Role.ASSISTANT, response, Role.ASSISTANT.name), null, 0)),
                    )
                }

            override fun tokensFromMessages(messages: List<Message>): Int {
                return 0
            }

            override val name: String = path.name

            override fun close(): Unit = gpt4allModel.close()

            override val modelType: ModelType = ModelType.LocalModel(name, EncodingType.CL100K_BASE, 4096)

            private fun List<Message>.buildPrompt(): String {
                val messages: String = joinToString("") { message ->
                    when (message.role) {
                        Role.SYSTEM -> message.content
                        Role.USER -> "\n### Human: ${message.content}"
                        Role.ASSISTANT -> "\n### Response: ${message.content}"
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


