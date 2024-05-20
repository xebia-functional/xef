package org.xef.xefMobile.services

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.xef.xefMobile.network.client.HttpClientProvider
import org.xef.xefMobile.model.*

class UserRepositoryService {
    private val client = HttpClientProvider.client
    private val baseUrl = "https://api.miservidor.com"
    private val json = Json { isLenient = true; ignoreUnknownKeys = true }

    suspend fun register(request: RegisterRequest): RegisterResponse = withContext(Dispatchers.IO) {
        client.post {
            url("$baseUrl/register")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun login(request: LoginRequest): LoginResponse = withContext(Dispatchers.IO) {
        client.post {
            url("$baseUrl/login")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
