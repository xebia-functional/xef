package com.server.movile.xef.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xef.xefMobile.model.Assistant
import com.xef.xefMobile.services.ApiService
import com.xef.xefMobile.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
}
