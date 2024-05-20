package com.server.movile.xef.android.ui.viewmodels

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.xef.xefMobile.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xef.xefMobile.services.ApiService
import retrofit2.HttpException

import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "settings")

class AuthViewModel(
    context: Context,
    private val apiService: ApiService
) : ViewModel(), IAuthViewModel {

    private val dataStore = context.dataStore

    private val _authToken = MutableLiveData<String?>()
    override val authToken: LiveData<String?> = _authToken

    private val _isLoading = MutableLiveData<Boolean>()
    override val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    override val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadAuthToken()
    }

    private fun loadAuthToken() {
        viewModelScope.launch {
            val token = dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("authToken")]
            }.firstOrNull()
            _authToken.value = token
        }
    }

    override fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val loginRequest = LoginRequest(email, password)
            try {
                val loginResponse = apiService.loginUser(loginRequest)
                updateAuthToken(loginResponse.authToken)
                _authToken.value = loginResponse.authToken
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

    override fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val request = RegisterRequest(name, email, password)
            try {
                val registerResponse = apiService.registerUser(request)
                updateAuthToken(registerResponse.authToken)
                _authToken.value = registerResponse.authToken
            } catch (e: Exception) {
                handleException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleException(e: Exception) {
        when (e) {
            is IOException -> _errorMessage.value = "Network error"
            is HttpException -> _errorMessage.value = "Unexpected server error"
            else -> _errorMessage.value = "An unexpected error occurred"
        }
    }

    override fun signout() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("authToken")] = ""
            }
            _authToken.value = null
        }
    }
}
