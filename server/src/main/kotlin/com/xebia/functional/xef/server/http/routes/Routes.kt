package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.auto.llm.openai.*
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.server.services.PersistenceService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

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
                    prompt = Prompt(data.messages, PromptConfiguration(
                        temperature = data.temperature,
                        numberOfPredictions = data.n,
                        user = data.user ?: ""
                    )
                    ),
                    scope = scope
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
