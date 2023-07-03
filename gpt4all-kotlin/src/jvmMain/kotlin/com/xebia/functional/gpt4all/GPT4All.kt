package com.xebia.functional.gpt4all

import ai.djl.training.util.DownloadUtils
import ai.djl.training.util.ProgressBar
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
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.io.path.name

interface GPT4All : AutoCloseable, Chat, Completion {

  val gpt4allModel: GPT4AllModel

  override fun close() {
  }

  companion object {

    operator fun invoke(
      url: String,
      path: Path,
      modelType: LLModel.Type,
      generationConfig: GenerationConfig = GenerationConfig(),
    ): GPT4All = object : GPT4All {

      init {
        if (!Files.exists(path)) {
          DownloadUtils.download(url, path.toFile().absolutePath , ProgressBar())
        }
      }

      override val gpt4allModel = GPT4AllModel.invoke(path, modelType)

      override suspend fun createCompletion(request: CompletionRequest): CompletionResult =
        with(request) {
          val response: String = gpt4allModel.prompt(prompt, llmModelContext(generationConfig))
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
          val response: String =
            gpt4allModel.prompt(messages.buildPrompt(), llmModelContext(generationConfig))
          return ChatCompletionResponse(
            UUID.randomUUID().toString(),
            path.name,
            System.currentTimeMillis().toInt(),
            path.name,
            Usage.ZERO,
            listOf(Choice(Message(Role.ASSISTANT, response, Role.ASSISTANT.name), null, 0)),
          )
        }

      override fun tokensFromMessages(messages: List<Message>): Int = 0

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

      private fun llmModelContext(generationConfig: GenerationConfig): LLModelContext =
        with(generationConfig) {
          LLModelContext(
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
        }
    }

  }
}

