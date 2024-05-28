package com.xef.xefMobile.services

import com.xef.xefMobile.model.LoginRequest
import com.xef.xefMobile.model.LoginResponse
import com.xef.xefMobile.model.RegisterRequest
import com.xef.xefMobile.model.RegisterResponse
import com.xef.xefMobile.network.client.HttpClientProvider
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class UserRepositoryService {
  private val client = HttpClientProvider.client
  private val baseUrl = "https://api.miservidor.com"
  private val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
  }

  suspend fun register(request: RegisterRequest): RegisterResponse =
    withContext(Dispatchers.IO) {
      client
        .post {
          url("$baseUrl/register")
          contentType(ContentType.Application.Json)
          setBody(request)
        }
        .body()
    }

  suspend fun login(request: LoginRequest): LoginResponse =
    withContext(Dispatchers.IO) {
      client
        .post {
          url("$baseUrl/login")
          contentType(ContentType.Application.Json)
          setBody(request)
        }
        .body()
    }
}
