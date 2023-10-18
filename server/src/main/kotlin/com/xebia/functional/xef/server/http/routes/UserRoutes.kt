package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.LoginRequest
import com.xebia.functional.xef.server.models.RegisterRequest
import com.xebia.functional.xef.server.services.UserRepositoryService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Routing.userRoutes(userRepositoryService: UserRepositoryService) {
  post("/register") {
    val request = Json.decodeFromString<RegisterRequest>(call.receive<String>())
    val response = userRepositoryService.register(request)
    call.respond(response)
  }

  post("/login") {
    val request = Json.decodeFromString<LoginRequest>(call.receive<String>())
    val response = userRepositoryService.login(request)
    call.respond(response)
  }
}
