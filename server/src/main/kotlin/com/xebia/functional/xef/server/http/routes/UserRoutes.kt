package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.LoginRequest
import com.xebia.functional.xef.server.models.LoginResponse
import com.xebia.functional.xef.server.models.RegisterRequest
import com.xebia.functional.xef.server.services.UserRepositoryService
import guru.zoroark.tegral.openapi.dsl.schema
import guru.zoroark.tegral.openapi.ktor.resources.ResourceDescription
import guru.zoroark.tegral.openapi.ktor.resources.describeResource
import guru.zoroark.tegral.openapi.ktor.resources.postD
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.userRoutes(userRepositoryService: UserRepositoryService) {
  postD<UserRegisterRoutes> {
    val request = call.decodeFromStringRequest<RegisterRequest>()
    val response = userRepositoryService.register(request)
    call.respond(response)
  }

  postD<UserLoginRoutes> {
    val request = call.decodeFromStringRequest<LoginRequest>()
    val response = userRepositoryService.login(request)
    call.respond(response)
  }
}

@Resource("/register")
class UserRegisterRoutes {
  companion object :
    ResourceDescription by describeResource({
      tags += "User"
      post {
        description = "Register a new user"
        body {
          description = "The user details"
          required = true
          json { schema<RegisterRequest>() }
        }
        HttpStatusCode.OK.value response
          {
            description = "User registered successfully"
            json { schema<LoginResponse>() }
          }
      }
    })
}

@Resource("/login")
class UserLoginRoutes {
  companion object :
    ResourceDescription by describeResource({
      tags += "User"
      post {
        description = "Login a user"
        body {
          description = "The user details"
          required = true
          json { schema<LoginRequest>() }
        }
        HttpStatusCode.OK.value response
          {
            description = "User logged in successfully"
            json { schema<LoginResponse>() }
          }
      }
    })
}
