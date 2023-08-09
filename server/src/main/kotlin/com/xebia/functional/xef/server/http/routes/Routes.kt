package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatRole
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.auto.llm.openai.*
import com.xebia.functional.xef.auto.llm.openai.OpenAI.Companion.DEFAULT_CHAT
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest as XefChatCompletionRequest

@OptIn(BetaOpenAI::class)
fun Routing.routes() {
    authenticate("auth-bearer") {
        post("/chat/completions") {
            val model: Chat = call.request.headers["xef-model"]?.let {
                it.toOpenAIModel()
            } ?: DEFAULT_CHAT
            val token = call.principal<UserIdPrincipal>()?.name ?: throw IllegalArgumentException("No token found")
            val scope = CoreAIScope(OpenAIEmbeddings(OpenAI(token).TEXT_EMBEDDING_ADA_002))
            val data = call.receive<ChatCompletionRequest>().toCore()
            response<String, Throwable> {
                model.promptMessage(
                    question = data.messages.joinToString("\n") { "${it.role}: ${it.content}" },
                    context = scope.context,
                    promptConfiguration = PromptConfiguration(
                        temperature = data.temperature,
                        numberOfPredictions = data.n,
                        user = data.user ?: ""
                    )
                )
            }
        }
    }
}


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

@OptIn(BetaOpenAI::class)
private fun ChatCompletionRequest.toCore(): XefChatCompletionRequest = XefChatCompletionRequest(
    model = model.id,
    messages = messages.map { Message(it.role.toCore(), it.content ?: "", it.name ?: "") },
    temperature = temperature ?: 0.0,
    topP = topP ?: 1.0,
    n = n ?: 1,
    stream = false,
    stop = stop,
    maxTokens = maxTokens,
    presencePenalty = presencePenalty ?: 0.0,
    frequencyPenalty = frequencyPenalty ?: 0.0,
    logitBias = logitBias ?: emptyMap(),
    user = user,
    streamToStandardOut = false
)

@OptIn(BetaOpenAI::class)
private fun ChatRole.toCore(): Role =
    when (this) {
        ChatRole.System -> Role.SYSTEM
        ChatRole.User -> Role.USER
        ChatRole.Assistant -> Role.ASSISTANT
        else -> Role.ASSISTANT
    }
