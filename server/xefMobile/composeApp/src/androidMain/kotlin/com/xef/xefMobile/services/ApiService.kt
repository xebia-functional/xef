package com.xef.xefMobile.services

import android.util.Log
import com.xef.xefMobile.model.*
import com.xef.xefMobile.network.client.HttpClientProvider
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class ApiService {

    suspend fun registerUser(request: RegisterRequest): RegisterResponse {
        return try {
            HttpClientProvider.client.post {
                url("http://10.0.2.2:8081/register")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            Log.e("ApiService", "Register failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun loginUser(request: LoginRequest): LoginResponse {
        return try {
            val response: HttpResponse = HttpClientProvider.client.post {
                url("http://10.0.2.2:8081/login")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val responseBody: String = response.bodyAsText()
            Log.d("ApiService", "Login response body: $responseBody")

            response.body()
        } catch (e: Exception) {
            Log.e("ApiService", "Login failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun getAssistants(authToken: String): AssistantsResponse {
        return try {
            val response: HttpResponse = HttpClientProvider.client.get {
                url("http://10.0.2.2:8081/v1/settings/assistants")
                header(HttpHeaders.Authorization, "Bearer $authToken")
                header("OpenAI-Beta", "assistants=v1")
            }

            val responseBody: String = response.bodyAsText()
            Log.d("ApiService", "Assistants response body: $responseBody")

            response.body()
        } catch (e: Exception) {
            Log.e("ApiService", "Fetching assistants failed: ${e.message}", e)
            throw e
        }
    }
}
