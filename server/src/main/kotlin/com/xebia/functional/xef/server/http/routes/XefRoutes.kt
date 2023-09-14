package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.MessagePolicy
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.MemoryManagement
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.prompt.templates.assistant
import com.aallam.openai.api.BetaOpenAI
import com.xebia.functional.xef.server.services.VectorStoreService
import com.xebia.functional.xef.store.ConversationId
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

@OptIn(BetaOpenAI::class)
fun Routing.genAIRoutes(
    client: HttpClient,
    vectorStoreService: VectorStoreService
) {
  val openAiUrl = "https://api.openai.com/v1"

    authenticate("auth-bearer") {
        post("/chat/completions") {
          try {
            val config = ChatConfig(call)
            val conversation = conversation(config, vectorStoreService)
            callModel(conversation, config)
          } catch (e: IllegalStateException) {
            call.respondText(e.message ?: "Model not found", status = HttpStatusCode.BadRequest)
          }
        }

    post("/embeddings") {
      val token = call.getToken()
      val context = call.receive<String>()
      client.makeRequest(call, "$openAiUrl/embeddings", context, token)
    }
  }
}

private fun promptConfiguration(coreRequest: com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest): PromptConfiguration =
  PromptConfiguration(
    maxDeserializationAttempts = 3,
    user = coreRequest.user ?: Role.USER.name,
    temperature = coreRequest.temperature,
    numberOfPredictions = coreRequest.n,
    docsInContext = 5,
    minResponseTokens = coreRequest.maxTokens ?: 500,
    messagePolicy = MessagePolicy()
  )

private suspend fun callModel(conversation: Conversation, config: ChatConfig) {
  when (val model = config.model) {
    is Chat -> {
      val coreRequest = config.openaiRequest.toCore(config.stream)
      val promptConfiguration = promptConfiguration(coreRequest)
      val prompt = Prompt(messages = coreRequest.messages, configuration = promptConfiguration)
      val request = model.chatRequest(prompt = prompt, conversation = conversation, stream = config.stream)
      if (config.stream) {
        streamResponse(config, model, request, conversation)
      } else {
        val response = model.createChatCompletion(request)
        val json = Json.encodeToJsonElement(response).toString()
        config.call.respond(json)
      }
    }
    else -> error("Unsupported model type: ${model::class.simpleName}")
  }
}

private suspend fun streamResponse(
  config: ChatConfig,
  model: Chat,
  request: com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest,
  conversation: Conversation
) {
  config.call.respondBytesWriter(contentType = ContentType.Application.Json) {
    model.createChatCompletions(request)
      .onEach {
        try {
          val json = Json.encodeToJsonElement(it).toString()
          writeStringUtf8(json)
          flush()
        } catch (_: Exception) {
          // ignore
        }
      }
      .fold("") { acc, chunk ->
        val text = chunk.choices.mapNotNull { it.delta?.content }.reduceOrNull(String::plus) ?: ""
        acc + text
      }
      .also { finalText ->
        val message = assistant(finalText)
        MemoryManagement.addMemoriesAfterStream(model, request, conversation, listOf(message))
      }
  }
}

private fun conversation(
  config: ChatConfig,
  vectorStoreService: VectorStoreService
): PlatformConversation {
  val conversationId = config.conversationId?.let(::ConversationId)
  val conversation = Conversation(
    conversationId = conversationId,
    store = vectorStoreService.getVectorStore(config.provider, config.token),
  )
  return conversation
}

private suspend fun HttpClient.makeRequest(
  call: ApplicationCall,
  url: String,
  body: String,
  token: String
) {
  val response = this.request(url) {
    headers {
      bearerAuth(token)
    }
    contentType(ContentType.Application.Json)
    method = HttpMethod.Post
    setBody(body)
  }
  call.response.headers.copyFrom(response.headers)
  call.respond(response.status, response.body<String>())
}

private suspend fun HttpClient.makeStreaming(
  call: ApplicationCall,
  url: String,
  body: String,
  token: String
) {
  this.preparePost(url) {
    headers {
      bearerAuth(token)
    }
    contentType(ContentType.Application.Json)
    method = HttpMethod.Post
    setBody(body)
  }.execute { httpResponse ->
    call.response.headers.copyFrom(httpResponse.headers)
    call.respondOutputStream {
      httpResponse
        .bodyAsChannel()
        .copyTo(this@respondOutputStream)
    }
  }
}

private fun ResponseHeaders.copyFrom(headers: Headers) = headers
  .entries()
  .filter { (key, _) -> !HttpHeaders.isUnsafe(key) } // setting unsafe headers results in exception
  .forEach { (key, values) ->
    values.forEach { value -> this.appendIfAbsent(key, value) }
  }

private fun ApplicationCall.getProvider(): Provider =
    request.headers["xef-provider"]?.toProvider()
        ?: Provider.OPENAI

fun ApplicationCall.getToken(): String =
  principal<UserIdPrincipal>()?.name ?: throw IllegalArgumentException("No token found")

fun ApplicationCall.getConversationId(): String? =
  request.headers["xef-conversation-id"]

/**
 * Responds with the data and converts any potential Throwable into a 404.
 */
private suspend inline fun <reified T : Any, E : Throwable> PipelineContext<*, ApplicationCall>.response(
  block: () -> T
) = arrow.core.raise.recover<E, Unit>({
  call.respond(block())
}) {
  call.respondText(it.message ?: "Response not found", status = HttpStatusCode.NotFound)
}
