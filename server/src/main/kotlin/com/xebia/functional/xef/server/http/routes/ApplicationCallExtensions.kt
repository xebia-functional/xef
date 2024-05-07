package com.xebia.functional.xef.server.http.routes

import arrow.core.raise.catch
import com.xebia.functional.xef.server.models.Token
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json

fun ApplicationCall.getToken(): Token =
  principal<UserIdPrincipal>()?.name?.let { Token(it) }
    ?: throw XefExceptions.AuthorizationException("No token found")

fun ApplicationCall.getId(): Int = getInt("id")

fun ApplicationCall.getInt(field: String): Int =
  this.parameters[field]?.toInt() ?: throw XefExceptions.ValidationException("Invalid $field")

suspend inline fun <reified T> ApplicationCall.decodeFromStringRequest(): T =
  catch({ Json.decodeFromString<T>(this.receive<String>()) }) {
    throw XefExceptions.ValidationException("Invalid ${T::class.simpleName}")
  }
