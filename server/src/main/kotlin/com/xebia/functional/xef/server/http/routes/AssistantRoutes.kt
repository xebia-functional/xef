package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.openai.generated.model.CreateAssistantRequest
import com.xebia.functional.openai.generated.model.ModifyAssistantRequest
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.assistants.Assistant
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.assistantRoutes(logger: KLogger) {
  authenticate("auth-bearer") {
    post("/v1/settings/assistants") {
      try {
        val contentType = call.request.contentType()
        if (contentType == ContentType.Application.Json) {
          val request = call.receive<CreateAssistantRequest>()
          val assistant = Assistant(request)
          val response = assistant.get()
          logger.info { "Created assistant: ${response.name} with id: ${response.id}" }
          call.respond(status = HttpStatusCode.Created, response)
        } else {
          call.respond(
            HttpStatusCode.UnsupportedMediaType,
            "Unsupported content type: $contentType"
          )
        }
      } catch (e: Exception) {
        val trace = e.stackTraceToString()
        call.respond(HttpStatusCode.BadRequest, "Invalid request: $trace")
      }
    }

    get("/v1/settings/assistants") {
      try {
        val token = call.getToken()
        val openAI = OpenAI(Config(token = token.value), logRequests = true)
        val assistantsApi = openAI.assistants
        val response =
          assistantsApi.listAssistants(configure = { header("OpenAI-Beta", "assistants=v2") })
        call.respond(HttpStatusCode.OK, response)
      } catch (e: Exception) {
        val trace = e.stackTraceToString()
        logger.error { "Error creating assistant: $trace" }
        call.respond(HttpStatusCode.BadRequest, "Invalid request: $trace")
      }
    }

    put("/v1/settings/assistants/{id}") {
      try {
        val contentType = call.request.contentType()
        if (contentType == ContentType.Application.Json) {
          val request = call.receive<ModifyAssistantRequest>()
          val id = call.parameters["id"]
          if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid assistant id")
            return@put
          }
          val assistant = Assistant(id)
          val response = assistant.modify(request).get()
          logger.info { "Modified assistant: ${response.name} with id: ${response.id}" }
          call.respond(HttpStatusCode.OK, response)
        } else {
          call.respond(
            HttpStatusCode.UnsupportedMediaType,
            "Unsupported content type: $contentType"
          )
        }
      } catch (e: Exception) {
        val trace = e.stackTraceToString()
        logger.error { "Error modifying assistant: $trace" }
        call.respond(HttpStatusCode.BadRequest, "Invalid request: $trace")
      }
    }

    delete("/v1/settings/assistants/{id}") {
      try {
        val token = call.getToken()
        val id = call.parameters["id"]
        if (id == null) {
          call.respond(HttpStatusCode.BadRequest, "Invalid assistant id")
          return@delete
        }
        val openAI = OpenAI(Config(token = token.value), logRequests = true)
        val assistantsApi = openAI.assistants
        val assistant = assistantsApi.getAssistant(id)
        val response =
          assistantsApi.deleteAssistant(id, configure = { header("OpenAI-Beta", "assistants=v2") })
        logger.info { "Deleted assistant: ${assistant.name} with id: ${response.id}" }
        call.respond(status = HttpStatusCode.NoContent, response)
      } catch (e: Exception) {
        val trace = e.stackTraceToString()
        logger.error { "Error deleting assistant: $trace" }
        call.respond(HttpStatusCode.BadRequest, "Invalid request: $trace")
      }
    }
  }
}
