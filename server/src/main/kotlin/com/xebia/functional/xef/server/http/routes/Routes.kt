package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.llm.openai.getOrThrow
import com.xebia.functional.xef.auto.llm.openai.promptMessage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Routing.routes() {
    post("/prompt/message") {
        val provider = call.request.headers["provider"]?.let {
            it
        } ?: "OpenAI"
        println("provider: $provider")
        val data = call.receive<PromptMessageRequest>()
        response<String, Throwable> {
            ai {
                promptMessage(data.message)
            }.getOrThrow()
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
