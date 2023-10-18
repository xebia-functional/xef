package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.TokenRequest
import com.xebia.functional.xef.server.models.TokenUpdateRequest
import com.xebia.functional.xef.server.services.TokenRepositoryService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Routing.tokensRoutes(tokenRepositoryService: TokenRepositoryService) {
  authenticate("auth-bearer") {
    get("/v1/settings/tokens") {
      val token = call.getToken()
      val response = tokenRepositoryService.getTokens(token)
      call.respond(response)
    }
    get("/v1/settings/tokens/projects/{id}") {
      val token = call.getToken()
      val id = call.getId()
      val response = tokenRepositoryService.getTokensByProject(token, id)
      call.respond(response)
    }
    post("/v1/settings/tokens") {
      val request = Json.decodeFromString<TokenRequest>(call.receive<String>())
      val token = call.getToken()
      val response = tokenRepositoryService.createToken(request, token)
      call.respond(status = HttpStatusCode.Created, response)
    }
    put("/v1/settings/tokens/{id}") {
      val request = Json.decodeFromString<TokenUpdateRequest>(call.receive<String>())
      val token = call.getToken()
      val id = call.getId()
      val response = tokenRepositoryService.updateToken(token, request, id)
      call.respond(status = HttpStatusCode.NoContent, response)
    }
    delete("/v1/settings/tokens/{id}") {
      val token = call.getToken()
      val id = call.getId()
      val response = tokenRepositoryService.deleteToken(token, id)
      call.respond(status = HttpStatusCode.NoContent, response)
    }
  }
}
