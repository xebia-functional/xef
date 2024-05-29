package com.server.movile.xef.android.ui.viewmodels

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xef.xefMobile.model.LoginRequest
import com.xef.xefMobile.model.RegisterRequest
import com.xef.xefMobile.services.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "settings")

class AuthViewModel(context: Context, private val apiService: ApiService) : ViewModel(), IAuthViewModel {

  private val dataStore = context.dataStore

  private val _authToken = MutableLiveData<String?>()
  override val authToken: LiveData<String?> = _authToken

  private val _isLoading = MutableLiveData<Boolean>()
  override val isLoading: LiveData<Boolean> = _isLoading

  private val _errorMessage = MutableLiveData<String?>()
  override val errorMessage: LiveData<String?> = _errorMessage

  private val _userName = MutableLiveData<String?>()
  override val userName: LiveData<String?> = _userName

  private val _loginError = MutableLiveData<String?>()
  override val loginError: LiveData<String?> = _loginError

  init {
    loadAuthToken()
  }

  private fun loadAuthToken() {
    viewModelScope.launch {
      val token = dataStore.data
        .map { preferences -> preferences[stringPreferencesKey("authToken")] }
        .firstOrNull()
      _authToken.value = token

      token?.let { loadUserName() }
    }
  }

  private suspend fun loadUserName() {
    withContext(Dispatchers.IO) {
      val name = dataStore.data
        .map { preferences -> preferences[stringPreferencesKey("userName")] }
        .firstOrNull()
      _userName.postValue(name)
    }
  }

  override fun login(email: String, password: String) {
    viewModelScope.launch {
      _isLoading.value = true
      val loginRequest = LoginRequest(email, password)
      try {
        val loginResponse = apiService.loginUser(loginRequest)
        updateAuthToken(loginResponse.authToken)
        updateUserName(loginResponse.user.name)
        _authToken.value = loginResponse.authToken
        _userName.value = loginResponse.user.name
        _loginError.value = null
      } catch (e: Exception) {
        handleException(e)
      } finally {
        _isLoading.value = false
      }
    }
  }

  private suspend fun updateAuthToken(token: String) {
    withContext(Dispatchers.IO) {
      dataStore.edit { preferences ->
        preferences[stringPreferencesKey("authToken")] = token
      }
    }
  }

  private suspend fun updateUserName(name: String) {
    withContext(Dispatchers.IO) {
      dataStore.edit { preferences ->
        preferences[stringPreferencesKey("userName")] = name
      }
    }
  }

  override fun register(name: String, email: String, password: String) {
    viewModelScope.launch {
      _isLoading.value = true
      val request = RegisterRequest(name, email, password)
      try {
        val registerResponse = apiService.registerUser(request)
        updateAuthToken(registerResponse.authToken)
        updateUserName(name)
        _authToken.value = registerResponse.authToken
        _userName.value = name
      } catch (e: Exception) {
        handleException(e)
      } finally {
        _isLoading.value = false
      }
    }
  }

  private fun handleException(e: Exception) {
    val errorMessage = when (e) {
      is IOException -> "Network error"
      is HttpException -> {
        when (e.code()) {
          401 -> "Incorrect email or password"
          404 -> "Email not registered"
          else -> "Unexpected server error"
        }
      }
      else -> "An unexpected error occurred"
    }
    _loginError.postValue(errorMessage)
  }

  override fun logout() {
    viewModelScope.launch {
      try {
        withContext(Dispatchers.IO) {
          dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey("authToken"))
            preferences.remove(stringPreferencesKey("userName"))
          }
        }
        _authToken.postValue(null)
        _userName.postValue(null)
        _errorMessage.postValue("Logged out successfully")
      } catch (e: Exception) {
        _errorMessage.postValue("Failed to sign out")
      }
    }
  }
}
