package com.xef.xefMobile.services

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.xef.xefMobile.network.client.HttpClientProvider
import android.util.Log
import com.xef.xefMobile.model.LoginRequest
import com.xef.xefMobile.model.LoginResponse
import com.xef.xefMobile.model.RegisterRequest
import com.xef.xefMobile.model.RegisterResponse

class ApiService {

    suspend fun registerUser(request: RegisterRequest): RegisterResponse {
        return try {
            HttpClientProvider.client.post {
                url("http://10.0.2.2:8081/register")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            // Handle or log the exception as needed
            Log.e("ApiService", "Register failed: ${e.message}", e)
            throw e // Re-throwing the exception for now
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
            // Handle or log the exception as needed
            Log.e("ApiService", "Login failed: ${e.message}", e)
            throw e // Re-throwing the exception for now
        }
    }
}
