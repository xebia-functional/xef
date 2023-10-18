package com.xebia.functional.xef.server.exceptions

import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.exceptionsHandler() {
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      if (cause is XefExceptions) {
        call.manageException(cause)
      } else {
        call.respond(
          HttpStatusCode.InternalServerError,
          cause.localizedMessage ?: "Unexpected error"
        )
      }
    }
    status(HttpStatusCode.NotFound) { call, status ->
      call.respondText(text = "404: Page Not Found", status = status)
    }
  }
}

suspend fun ApplicationCall.manageException(cause: XefExceptions) {
  when (cause) {
    is XefExceptions.ValidationException -> this.respond(HttpStatusCode.BadRequest, cause.message)
    is XefExceptions.AuthorizationException -> this.respond(HttpStatusCode.Unauthorized)
    is XefExceptions.OrganizationsException ->
      this.respond(HttpStatusCode.BadRequest, cause.message)
    is XefExceptions.ProjectException -> this.respond(HttpStatusCode.BadRequest, cause.message)
    is XefExceptions.XefTokenException -> this.respond(HttpStatusCode.BadRequest, cause.message)
    is XefExceptions.UserException -> this.respond(HttpStatusCode.BadRequest, cause.message)
  }
}
