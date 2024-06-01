package ai.xef.langchain4j.streams

import ai.xef.stream.AIEvent
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ToolExecutionResultMessage
import dev.langchain4j.model.StreamingResponseHandler
import dev.langchain4j.model.output.FinishReason
import dev.langchain4j.model.output.Response
import dev.langchain4j.model.output.TokenUsage
import dev.langchain4j.service.AiServiceContext
import dev.langchain4j.service.ServiceOutputParser
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.trySendBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class KotlinAiServiceStreamingResponseHandler<A>(
  private val returnType: Class<*>,
  private val producer: ProducerScope<AIEvent<A>>,
  private val context: AiServiceContext,
  private val memoryId: Any,
  private val tokenUsage: TokenUsage,
  private val log: Logger = LoggerFactory.getLogger(KotlinAiServiceStreamingResponseHandler::class.java)
) : StreamingResponseHandler<AiMessage> {

  override fun onNext(token: String) {
    producer.trySendBlocking(AIEvent.Chunk(token))
  }

  override fun onComplete(response: Response<AiMessage>) {
    val aiMessage = response.content()

    if (context.hasChatMemory()) {
      context.chatMemory(memoryId).add(aiMessage)
    }

    if (aiMessage.hasToolExecutionRequests()) {
      for (toolExecutionRequest in aiMessage.toolExecutionRequests()) {
        producer.trySendBlocking(
          AIEvent.ToolRequest(
            toolExecutionRequest.id(),
            toolExecutionRequest.name(),
            toolExecutionRequest.arguments()
          )
        )
        val toolExecutor = context.toolExecutors[toolExecutionRequest.name()]
        val toolExecutionResult = toolExecutor!!.execute(toolExecutionRequest, memoryId)
        val toolExecutionResultMessage = ToolExecutionResultMessage.from(
          toolExecutionRequest,
          toolExecutionResult
        )
        producer.trySendBlocking(
          AIEvent.ToolResult(
            toolExecutionResultMessage.id(),
            toolExecutionResultMessage.toolName(),
            toolExecutionResultMessage.text()
          )
        )
        context.chatMemory(memoryId).add(toolExecutionResultMessage)
      }

      context.streamingChatModel.generate(
        context.chatMemory(memoryId).messages(),
        context.toolSpecifications,
        KotlinAiServiceStreamingResponseHandler(
          returnType,
          producer,
          context,
          memoryId,
          TokenUsage.sum(tokenUsage, response.tokenUsage())
        )
      )
    } else {
      val parsedResponse = ServiceOutputParser.parse(response, returnType)
      if (returnType.isInstance(parsedResponse)) {
        val usage = TokenUsage.sum(tokenUsage, response.tokenUsage())
        producer.trySendBlocking(
          AIEvent.Complete<A>(
            ai.xef.response.Response(
              parsedResponse as A,
              ai.xef.response.TokenUsage(
                usage.inputTokenCount(),
                usage.outputTokenCount(),
                usage.totalTokenCount()
              ),
              when (response.finishReason()) {
                FinishReason.STOP -> ai.xef.response.FinishReason.STOP
                FinishReason.LENGTH -> ai.xef.response.FinishReason.LENGTH
                FinishReason.TOOL_EXECUTION -> ai.xef.response.FinishReason.TOOL_EXECUTION
                FinishReason.CONTENT_FILTER -> ai.xef.response.FinishReason.CONTENT_FILTER
                FinishReason.OTHER -> ai.xef.response.FinishReason.OTHER
                null -> ai.xef.response.FinishReason.OTHER
              }
            )
          )
        )
      } else {
        log.error("Response type mismatch: expected $returnType, got ${parsedResponse.javaClass}")
        producer.trySendBlocking(AIEvent.Error(IllegalStateException("Response type mismatch: expected $returnType, got ${parsedResponse.javaClass}")))
      }
      producer.close()
    }
  }

  override fun onError(error: Throwable) {
    log.error("Error in AI response", error)
    producer.trySendBlocking(AIEvent.Error(error))
    throw error
  }
}
