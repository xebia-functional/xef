package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatRole
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.auto.llm.openai.*
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.server.services.PersistenceService
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest as XefChatCompletionRequest

enum class Provider {
    OPENAI, GPT4ALL, GCP
}

fun String.toProvider(): Provider? = when (this) {
    "openai" -> Provider.OPENAI
    "gpt4all" -> Provider.GPT4ALL
    "gcp" -> Provider.GCP
    else -> null
}


@OptIn(BetaOpenAI::class)
fun Routing.routes(persistenceService: PersistenceService) {
    authenticate("auth-bearer") {
        post("/chat/completions") {
            val provider: Provider = call.request.headers["xef-provider"]?.toProvider()
                ?: throw IllegalArgumentException("Not a valid provider")
            val token = call.principal<UserIdPrincipal>()?.name ?: throw IllegalArgumentException("No token found")
            val scope = Conversation(
                persistenceService.getVectorStore(provider, token)
            )
            val data = call.receive<ChatCompletionRequest>().toCore()
            val model: OpenAIModel = data.model.toOpenAIModel(token)
            response<String, Throwable> {
                model.promptMessage(
                    question = data.messages.joinToString("\n") { "${it.role}: ${it.content}" },
                    scope = scope,
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
