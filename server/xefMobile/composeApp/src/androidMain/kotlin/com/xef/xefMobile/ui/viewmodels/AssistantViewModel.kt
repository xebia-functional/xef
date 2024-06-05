package com.server.movile.xef.android.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xef.xefMobile.model.Assistant
import com.xef.xefMobile.services.ApiService
import com.xef.xefMobile.ui.viewmodels.SettingsViewModel
import io.ktor.client.statement.*
import io.ktor.http.*
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

  private val _selectedAssistant = MutableStateFlow<Assistant?>(null)
  val selectedAssistant: StateFlow<Assistant?> = _selectedAssistant.asStateFlow()

  private val apiService = ApiService()

  init {
    fetchAssistants()
  }

  fun fetchAssistants(onComplete: (() -> Unit)? = null) {
    viewModelScope.launch {
      _loading.value = true
      _errorMessage.value = null
      try {
        val token = settingsViewModel.apiKey.value ?: throw Exception("API key not found")
        val response = apiService.getAssistants(token)
        _assistants.value = response.data
        _loading.value = false
        onComplete?.invoke()
      } catch (e: Exception) {
        _errorMessage.value = "Failed to load assistants: ${e.message}"
        _loading.value = false
        onComplete?.invoke()
      }
    }
  }

  fun loadAssistantDetails(id: String) {
    fetchAssistants {
      viewModelScope.launch {
        val assistant = getAssistantById(id)
        if (assistant != null) {
          _selectedAssistant.value = assistant
          Log.d("AssistantViewModel", "Assistant details loaded: $assistant")
        } else {
          Log.d("AssistantViewModel", "Assistant not found with id: $id")
        }
      }
    }
  }

  private fun getAssistantById(id: String): Assistant? {
    return assistants.value.find { it.id == id }
  }

  @OptIn(InternalAPI::class)
  fun createAssistant(
    name: String,
    instructions: String,
    temperature: Float,
    topP: Float,
    model: String,
    fileSearchEnabled: Boolean,
    codeInterpreterEnabled: Boolean,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val token = settingsViewModel.apiKey.value ?: throw Exception("API key not found")
        val tools = mutableListOf<Tool>()
        if (fileSearchEnabled) tools.add(Tool(type = "file_search"))
        if (codeInterpreterEnabled) tools.add(Tool(type = "code_interpreter"))

        val request =
          CreateAssistantRequest(
            model = model,
            name = name,
            description = "This is an example assistant for testing purposes.",
            instructions = instructions,
            tools = tools,
            metadata = mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
            temperature = temperature,
            top_p = topP
          )

        val response: HttpResponse =
          apiService.createAssistant(authToken = token, request = request)

        if (response.status == HttpStatusCode.Created) {
          fetchAssistants()
          onSuccess()
        } else {
          onError("Failed to create assistant: ${response.status}")
        }
      } catch (e: Exception) {
        onError("Error: ${e.message ?: "Unknown error"}")
      }
    }
  }

  fun deleteAssistant(assistantId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
      try {
        val token = settingsViewModel.apiKey.value ?: throw Exception("API key not found")
        val response: HttpResponse = apiService.deleteAssistant(authToken = token, assistantId = assistantId)

        if (response.status == HttpStatusCode.NoContent) {
          fetchAssistants()
          onSuccess()
        } else {
          onError("Failed to delete assistant: ${response.status}")
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
  val description: String?,
  val instructions: String,
  val tools: List<Tool>,
  val metadata: Map<String, String>,
  val temperature: Float,
  val top_p: Float
)

@Serializable data class Tool(val type: String)
