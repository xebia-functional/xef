package com.server.movile.xef.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xef.xefMobile.model.Assistant
import com.xef.xefMobile.services.ApiService
import com.xef.xefMobile.ui.viewmodels.SettingsViewModel
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

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

  private val apiService = ApiService()

  init {
    fetchAssistants()
  }

  private fun fetchAssistants() {
    viewModelScope.launch {
      _loading.value = true
      _errorMessage.value = null
      try {
        val token = settingsViewModel.apiKey.value ?: throw Exception("API key not found")
        val response = apiService.getAssistants(token)
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
        val response: HttpResponse =
          apiService.createAssistant(
            authToken = token,
            request =
              CreateAssistantRequest(
                model = model,
                name = name,
                instructions = instructions,
                temperature = temperature.toDouble(),
                topP = topP.toDouble()
              )
          )
        if (response.status == HttpStatusCode.Created) {
          onSuccess()
        } else {
          onError("Failed to create assistant: ${response.status}")
        }
      } catch (e: Exception) {
        onError("Error: ${e.message ?: "Unknown error"}")
      }
    }
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
