package com.server.movile.xef.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xef.xefMobile.model.Assistant
import com.xef.xefMobile.services.ApiService
import com.xef.xefMobile.ui.viewmodels.SettingsViewModel
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssistantViewModel(
  private val authViewModel: IAuthViewModel,
  private val settingsViewModel: SettingsViewModel
) : ViewModel() {
  private val _assistants = MutableStateFlow<List<Assistant>>(emptyList())
  val assistants: StateFlow<List<Assistant>> = _assistants.asStateFlow()

  private val _loading = MutableStateFlow(true)
  val loading: StateFlow<Boolean> = _loading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val client = HttpClient()

  init {
    fetchAssistants()
  }

  private fun fetchAssistants() {
    viewModelScope.launch {
      _loading.value = true
      _errorMessage.value = null
      try {
        val token = settingsViewModel.apiKey.value ?: throw Exception("API key not found")
        val response = ApiService().getAssistants(token)
        _assistants.value = response.data
      } catch (e: Exception) {
        _errorMessage.value = "Failed to load assistants: ${e.message}"
      } finally {
        _loading.value = false
      }
    }
  }

  @OptIn(InternalAPI::class)
  fun createAssistant(
    name: String,
    instructions: String,
    temperature: Float,
    topP: Float,
    model: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val token = settingsViewModel.apiKey.value ?: throw Exception("API key not found")
        val response: HttpResponse = client.post("http://your.api.endpoint/v1/settings/assistants") {
          contentType(ContentType.Application.Json)
          header("Authorization", "Bearer $token")
          body = CreateAssistantRequest(
            model = model,
            name = name,
            instructions = instructions,
            temperature = temperature.toDouble(),
            topP = topP.toDouble()
          )
        }
        if (response.status == HttpStatusCode.Created) {
          onSuccess()
        } else {
          onError("Failed to create assistant: ${response.status}")
        }
      } catch (e: Exception) {
        onError("Error: ${e.message}")
      }
    }
  }

  private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = Date(timestamp * 1000) // Convert Unix timestamp to milliseconds
    return sdf.format(date)
  }
}

@Serializable
data class CreateAssistantRequest(
  val model: String,
  val name: String,
  val instructions: String,
  val temperature: Double,
  val topP: Double
)
