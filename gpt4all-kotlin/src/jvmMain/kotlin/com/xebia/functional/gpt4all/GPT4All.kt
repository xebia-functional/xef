package com.xebia.functional.gpt4all

import ai.djl.training.util.DownloadUtils
import ai.djl.training.util.ProgressBar
import com.hexadevlabs.gpt4all.LLModel
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.name


interface GPT4All : AutoCloseable, Chat, Completion {

  override fun close() {
  }

  companion object {

    operator fun invoke(
      url: String,
      path: Path
    ): GPT4All = object : GPT4All {

      init {
        if (!Files.exists(path)) {
          DownloadUtils.download(url, path.toFile().absolutePath, ProgressBar())
        }
      }

      val llModel = LLModel(path)

      override suspend fun createCompletion(request: CompletionRequest): CompletionResult =
        with(request) {

          val config = LLModel.config()
            .withTopP(request.topP?.toFloat() ?: 0.4f)
            .withTemp(request.temperature?.toFloat() ?: 0f)
            .withRepeatPenalty(request.frequencyPenalty.toFloat())
            .build()
          val response: String = generateCompletion(prompt, config, request.streamToStandardOut)
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
          val config = LLModel.config()
            .withTopP(request.topP.toFloat() ?: 0.4f)
            .withTemp(request.temperature.toFloat() ?: 0f)
            .withRepeatPenalty(request.frequencyPenalty.toFloat())
            .build()
          val response: String = generateCompletion(prompt, config, request.streamToStandardOut)
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

      override fun close(): Unit = llModel.close()

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
        config: LLModel.GenerationConfig,
        stream: Boolean,
      ): String {
        return llModel.generate(prompt, config, stream)
      }
    }

  }
}


