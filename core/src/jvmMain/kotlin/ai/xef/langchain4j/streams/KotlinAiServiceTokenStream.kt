package ai.xef.langchain4j.streams

import ai.xef.stream.AIEvent
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.internal.ValidationUtils
import dev.langchain4j.model.output.TokenUsage
import dev.langchain4j.service.AiServiceContext
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.*
import kotlin.coroutines.cancellation.CancellationException


class KotlinAiServiceTokenStream<out A>(
  private val returnType: Class<*>,
  private val messagesToSend: List<ChatMessage>,
  private val context: AiServiceContext,
  private val memoryId: Any
) {

  init {
    ValidationUtils.ensureNotNull(context.streamingChatModel, "streamingChatModel")
  }

  fun flow(): Flow<AIEvent<A>> = flow {
    channelFlow<AIEvent<A>> {
      try {
        context.streamingChatModel.generate(
          messagesToSend,
          context.toolSpecifications,
          KotlinAiServiceStreamingResponseHandler(
            returnType,
            this,
            context,
            memoryId,
            TokenUsage()
          )
        )
        awaitCancellation()
      } catch (e: CancellationException) {
        println("Flow cancelled")
      }
    }.collect {
      emit(it)
    }
  }
}
