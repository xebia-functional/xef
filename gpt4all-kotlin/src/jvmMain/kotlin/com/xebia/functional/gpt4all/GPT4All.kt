package com.xebia.functional.gpt4all

import ai.djl.training.util.DownloadUtils
import ai.djl.training.util.ProgressBar
import com.hexadevlabs.gpt4all.LLModel
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.name


interface GPT4All : AutoCloseable, Chat, Completion {

  override fun close() {
  }

  companion object {

    @JvmSynthetic
    suspend inline fun <A> conversation(
      store: VectorStore,
      noinline block: suspend Conversation.() -> A
    ): A = block(conversation(store))

    @JvmSynthetic
    suspend fun <A> conversation(
      block: suspend Conversation.() -> A
    ): A = block(conversation(LocalVectorStore(HuggingFaceLocalEmbeddings.DEFAULT)))

    @JvmStatic
    @JvmOverloads
    fun conversation(
      store: VectorStore = LocalVectorStore(HuggingFaceLocalEmbeddings.DEFAULT)
    ): PlatformConversation = Conversation(store, provider = TODO("provider for gpt4all"))

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

      /**
       * Creates chat completions based on the given ChatCompletionRequest.
       *
       * hacks the System.out until https://github.com/nomic-ai/gpt4all/pull/1126 is accepted or merged
       *
       * @param request The ChatCompletionRequest containing the necessary information for creating completions.
       * @return A Flow of ChatCompletionChunk objects representing the generated chat completions.
       */
      override suspend fun createChatCompletions(request: ChatCompletionRequest): Flow<ChatCompletionChunk> =
        with(request) {
          val prompt: String = messages.buildPrompt()
          val config = LLModel.config()
            .withTopP(request.topP.toFloat())
            .withTemp(request.temperature.toFloat())
            .withRepeatPenalty(request.frequencyPenalty.toFloat())
            .build()

          val originalOut = System.out // Save the original standard output

          return coroutineScope {
            val channel = Channel<String>(capacity = UNLIMITED)

            val outputStream = object : OutputStream() {
              override fun write(b: Int) {
                val c = b.toChar()
                channel.trySend(c.toString())
              }
            }

            val printStream = PrintStream(outputStream, true, StandardCharsets.UTF_8)

            fun toChunk(text: String?): ChatCompletionChunk =
              ChatCompletionChunk(
                UUID.randomUUID().toString(),
                System.currentTimeMillis().toInt(),
                path.name,
                listOf(ChatChunk(delta = ChatDelta(Role.ASSISTANT, text))),
                Usage.ZERO,
              )

            val flow = channel.consumeAsFlow().map { toChunk(it) }

            launch(Dispatchers.IO) {
              System.setOut(printStream) // Set the standard output to the print stream
              generateCompletion(prompt, config, request.streamToStandardOut)
              channel.close()
            }

            flow.onCompletion {
              System.setOut(originalOut) // Restore the original standard output
            }
          }
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
