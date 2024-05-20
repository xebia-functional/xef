package org.xef.xefMobile.services


import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.xef.xefMobile.network.client.HttpClientProvider
import org.xef.xefMobile.model.*

class UserRepositoryService {
    private val client = HttpClientProvider.client
    private val baseUrl = "https://api.miservidor.com"
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    private inline fun <reified T> Json.encodeToString(data: T): String {
        return encodeToString(serializer(), data)
    }

    private inline fun <reified T> Json.decodeFromString(data: String): T {
        return decodeFromString(serializer(), data)
    }

    suspend fun register(request: RegisterRequest): RegisterResponse = withContext(Dispatchers.IO) {
        val requestBody = json.encodeToString(request)
        val response = client.post("$baseUrl/register") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        json.decodeFromString(response.bodyAsText())
    }

    suspend fun login(request: LoginRequest): LoginResponse = withContext(Dispatchers.IO) {
        val requestBody = json.encodeToString(request)
        val response = client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        json.decodeFromString(response.bodyAsText())
    }
}