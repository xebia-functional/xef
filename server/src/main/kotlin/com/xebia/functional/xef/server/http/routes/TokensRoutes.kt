package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.TokenFullResponse
import com.xebia.functional.xef.server.models.TokenRequest
import com.xebia.functional.xef.server.models.TokenSimpleResponse
import com.xebia.functional.xef.server.models.TokenUpdateRequest
import com.xebia.functional.xef.server.services.TokenRepositoryService
import guru.zoroark.tegral.openapi.dsl.schema
import guru.zoroark.tegral.openapi.ktor.resources.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.swagger.v3.oas.models.media.StringSchema

fun Routing.tokensRoutes(tokenRepositoryService: TokenRepositoryService) {
  authenticate("auth-bearer") {
    getD<TokensRoutes> {
      val token = call.getToken()
      val response = tokenRepositoryService.getTokens(token)
      call.respond(response)
    }
    postD<TokensRoutes> {
      val request = call.decodeFromStringRequest<TokenRequest>()
      val token = call.getToken()
      val response = tokenRepositoryService.createToken(request, token)
      call.respond(status = HttpStatusCode.Created, response)
    }
    putD<TokenDetailsRoutes> {
      val request = call.decodeFromStringRequest<TokenUpdateRequest>()
      val token = call.getToken()
      val id = call.getId()
      val response = tokenRepositoryService.updateToken(token, request, id)
      call.respond(status = HttpStatusCode.NoContent, response)
    }
    deleteD<TokenDetailsRoutes> {
      val token = call.getToken()
      val id = call.getId()
      val response = tokenRepositoryService.deleteToken(token, id)
      call.respond(status = HttpStatusCode.NoContent, response)
    }
    getD<TokensByProjectRoutes> {
      val token = call.getToken()
      val id = call.getId()
      val response = tokenRepositoryService.getTokensByProject(token, id)
      call.respond(response)
    }
  }
}

@Resource("/v1/settings/tokens")
class TokensRoutes {
  companion object :
    ResourceDescription by describeResource({
      tags += "Tokens"
      get {
        description = "Get all tokens"
        HttpStatusCode.OK.value response
          {
            description = "The list of tokens"
            json { schema<TokenFullResponse>() }
          }
      }
      post {
        description = "Create a new token"
        body {
          description = "The token to create"
          required = true
          json { schema<TokenRequest>() }
        }
        HttpStatusCode.Created.value response
          {
            description = "The created token"
            json { schema<TokenSimpleResponse>() }
          }
      }
    })
}

@Resource("/v1/settings/tokens/{id}")
class TokenDetailsRoutes {
  companion object :
    ResourceDescription by describeResource({
      tags += "Tokens"
      "id" pathParameter
        {
          description = "Token ID"
          required = true
          schema = StringSchema()
          example = "token_ABC_123"
        }
      put {
        description = "Update a token"
        body {
          description = "The token to update"
          required = true
          json { schema<TokenUpdateRequest>() }
        }
        HttpStatusCode.NoContent.value response
          {
            description = "The updated token"
            json { schema<TokenFullResponse>() }
          }
      }
      delete {
        description = "Delete a token"
        HttpStatusCode.NoContent.value response { description = "The deleted token" }
      }
    })
}

@Resource("/v1/settings/tokens/projects/{id}")
class TokensByProjectRoutes {
  companion object :
    ResourceDescription by describeResource({
      tags += "Tokens"
      "id" pathParameter
        {
          description = "Project ID"
          required = true
          schema = StringSchema()
          example = "project_ABC_123"
        }
      get {
        description = "Get all tokens for a project"
        HttpStatusCode.OK.value response
          {
            description = "The list of tokens"
            json { schema<TokenFullResponse>() }
          }
      }
    })
}
