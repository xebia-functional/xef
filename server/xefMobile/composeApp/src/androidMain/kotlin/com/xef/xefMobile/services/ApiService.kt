package com.xef.xefMobile.services

import android.util.Log
import com.server.movile.xef.android.ui.viewmodels.CreateAssistantRequest
import com.server.movile.xef.android.ui.viewmodels.ModifyAssistantRequest
import com.xef.xefMobile.model.*
import com.xef.xefMobile.network.client.HttpClientProvider
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class ApiService {

  suspend fun registerUser(request: RegisterRequest): RegisterResponse {
    return try {
      HttpClientProvider.client
        .post {
          url("https://ace-asp-ghastly.ngrok-free.app/register")
          contentType(ContentType.Application.Json)
          setBody(request)
        }
        .body()
    } catch (e: Exception) {
      Log.e("ApiService", "Register failed: ${e.message}", e)
      throw e
    }
  }

  suspend fun loginUser(request: LoginRequest): LoginResponse {
    return try {
      val response: HttpResponse =
        HttpClientProvider.client.post {
          url("https://ace-asp-ghastly.ngrok-free.app/login")
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
      val response: HttpResponse =
        HttpClientProvider.client.get {
          url("https://ace-asp-ghastly.ngrok-free.app/v1/settings/assistants")
          header(HttpHeaders.Authorization, "Bearer $authToken")
          header("OpenAI-Beta", "assistants=v2")
        }

      val responseBody: String = response.bodyAsText()
      Log.d("ApiService", "Assistants response body: $responseBody")
      response.body()
    } catch (e: Exception) {
      Log.e("ApiService", "Fetching assistants failed: ${e.message}", e)
      throw e
    }
  }

  suspend fun createAssistant(authToken: String, request: CreateAssistantRequest): HttpResponse {
    return try {
      HttpClientProvider.client.post {
        url("https://ace-asp-ghastly.ngrok-free.app/v1/settings/assistants")
        contentType(ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $authToken")
        setBody(request)
      }
    } catch (e: Exception) {
      Log.e("ApiService", "Creating assistant failed: ${e.message}", e)
      throw e
    }
  }

  suspend fun deleteAssistant(authToken: String, assistantId: String): HttpResponse {
    return try {
      HttpClientProvider.client.delete {
        url("https://ace-asp-ghastly.ngrok-free.app/v1/settings/assistants/$assistantId")
        header(HttpHeaders.Authorization, "Bearer $authToken")
        header("OpenAI-Beta", "assistants=v2")
      }
    } catch (e: Exception) {
      Log.e("ApiService", "Deleting assistant failed: ${e.message}", e)
      throw e
    }
  }

  suspend fun updateAssistant(
    authToken: String,
    id: String,
    request: ModifyAssistantRequest
  ): HttpResponse {
    return try {
      HttpClientProvider.client.put {
        url("https://ace-asp-ghastly.ngrok-free.app/v1/settings/assistants/$id")
        contentType(ContentType.Application.Json)
        header(HttpHeaders.Authorization, "Bearer $authToken")
        setBody(request)
      }
    } catch (e: Exception) {
      Log.e("ApiService", "Updating assistant failed: ${e.message}", e)
      throw e
    }
  }
}
