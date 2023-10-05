package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.Token
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import io.ktor.server.application.*
import io.ktor.server.auth.*

private fun ApplicationCall.getProvider(): Provider =
    request.headers["xef-provider"]?.toProvider()
        ?: Provider.OPENAI

fun ApplicationCall.getToken(): Token =
    principal<UserIdPrincipal>()?.name?.let { Token(it) } ?: throw XefExceptions.AuthorizationException("No token found")

fun ApplicationCall.getId(): Int = getInt("id")

fun ApplicationCall.getInt(field: String): Int =
    this.parameters[field]?.toInt() ?: throw XefExceptions.ValidationException("Invalid $field")