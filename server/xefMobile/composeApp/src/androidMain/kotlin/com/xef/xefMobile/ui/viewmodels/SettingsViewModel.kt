package com.xef.xefMobile.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(context: Context) : ViewModel() {
  private val preferences: SharedPreferences =
    context.getSharedPreferences("settings", Context.MODE_PRIVATE)
  private val _apiKey = MutableStateFlow(preferences.getString("api_key", "") ?: "")
  val apiKey: StateFlow<String> = _apiKey

  fun setApiKey(newApiKey: String) {
    _apiKey.value = newApiKey
  }

  fun saveApiKey() {
    viewModelScope.launch { preferences.edit().putString("api_key", _apiKey.value).apply() }
  }
}
