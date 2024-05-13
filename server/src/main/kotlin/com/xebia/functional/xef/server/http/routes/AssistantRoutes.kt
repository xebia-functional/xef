package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.openai.generated.model.AssistantObject
import com.xebia.functional.openai.generated.model.CreateAssistantRequest
import com.xebia.functional.openai.generated.model.ListAssistantsResponse
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.assistants.Assistant
import com.xebia.functional.xef.server.models.Token
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.assistantRoutes() {
  authenticate("auth-bearer") {
    post("/v1/settings/assistants") {
      try {
        val contentType = call.request.contentType()
        if (contentType == ContentType.Application.Json) {
          val request = call.receive<CreateAssistantRequest>()
          val token = call.getToken()
          val response = createAssistant(token, request)
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

    //        put("/v1/settings/assistants/{id}") {
    //            val request = Json.decodeFromString<AssistantRequest>(call.receive<String>())
    //            val token = call.getToken()
    //            val id = call.getId()
    //            val response = updateAssistant(token, request, id)
    //            call.respond(status = HttpStatusCode.NoContent, response)
    //        }
    // get("/v1/settings/assistants") {
    //      val token = call.getToken()
    //      val response = ListAssistantsResponse("list", emptyList(), null, null, false)
    //      val assistantResponse = listAssistants(token, response)
    //      call.respond(assistantResponse)
    //    }
    //        delete("/v1/settings/assistants/{id}") {
    //            val token = call.getToken()
    //            val id = call.parameters["id"]?.toIntOrNull()
    //            if (id == null) {
    //                call.respond(HttpStatusCode.BadRequest, "Invalid assistant id")
    //                return@delete
    //            }
    //            val response = deleteAssistant(token, id)
    //            call.respond(status = HttpStatusCode.NoContent, response)
    //        }
  }
}

suspend fun createAssistant(token: Token, request: CreateAssistantRequest): AssistantObject {
  val openAIConfig = Config(token = token.value)
  val openAI = OpenAI(openAIConfig, logRequests = true)
  val assistants = openAI.assistants
  val assistant = Assistant(request)
  return assistant.get()
}

// suspend fun updateAssistant(token: String, request: AssistantRequest, id: Int): String {
//    // Implement the logic for updating an assistant in OpenAI here
// }

suspend fun listAssistants(token: Token, response: ListAssistantsResponse): ListAssistantsResponse {
  val openAIConfig = Config(token = token.value)
  val openAI = OpenAI(openAIConfig)
  val assistants = openAI.assistants
  val listAssistants = assistants.listAssistants()

  return listAssistants
}

/*suspend fun deleteAssistant(token: String, id: Int): String {
    // Implement the logic for deleting an assistant in OpenAI here
}*/
