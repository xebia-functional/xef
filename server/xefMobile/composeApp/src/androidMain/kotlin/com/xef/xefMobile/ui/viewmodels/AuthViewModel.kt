package com.server.movile.xef.android.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xef.xefMobile.model.LoginRequest
import com.xef.xefMobile.model.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.xef.xefMobile.services.ApiService
import retrofit2.HttpException
import java.io.IOException

// Extension function to provide DataStore instance
private val Context.dataStore by preferencesDataStore(name = "settings")

class AuthViewModel(
    context: Context,
    private val apiService: ApiService
) : ViewModel(), IAuthViewModel {

    private val dataStore = context.dataStore

    // MutableLiveData properties to manage state
    private val _authToken = MutableLiveData<String?>()
    override val authToken: LiveData<String?> = _authToken

    private val _isLoading = MutableLiveData<Boolean>()
    override val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    override val errorMessage: LiveData<String?> = _errorMessage

    // Initializing by loading the auth token
    init {
        loadAuthToken()
    }

    // Function to load the authentication token from DataStore
    private fun loadAuthToken() {
        viewModelScope.launch {
            val token = dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("authToken")]
            }.firstOrNull()
            _authToken.value = token
        }
    }

    // Function to handle user login
    override fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val loginRequest = LoginRequest(email, password)
            try {
                Log.d("AuthViewModel", "Attempting login with email: $email")
                val loginResponse = apiService.loginUser(loginRequest)
                Log.d("AuthViewModel", "Login successful, token: ${loginResponse.authToken}")
                updateAuthToken(loginResponse.authToken)
                _authToken.value = loginResponse.authToken
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed: ${e.message}", e)
                handleException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to update the authentication token in DataStore
    private suspend fun updateAuthToken(token: String) {
        try {
            withContext(Dispatchers.IO) {
                dataStore.edit { preferences ->
                    preferences[stringPreferencesKey("authToken")] = token
                }
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Failed to update auth token")
        }
    }

    // Function to handle user registration
    override fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val request = RegisterRequest(name, email, password)
            try {
                Log.d("AuthViewModel", "Attempting registration with email: $email")
                val registerResponse = apiService.registerUser(request)
                Log.d("AuthViewModel", "Registration successful, token: ${registerResponse.authToken}")
                updateAuthToken(registerResponse.authToken)
                _authToken.value = registerResponse.authToken
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration failed: ${e.message}", e)
                handleException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to handle exceptions
    private fun handleException(e: Exception) {
        when (e) {
            is IOException -> _errorMessage.postValue("Network error")
            is HttpException -> _errorMessage.postValue("Unexpected server error: ${e.code()}")
            else -> _errorMessage.postValue("An unexpected error occurred: ${e.message}")
        }
    }

    // Function to handle user logout
    override fun logout() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dataStore.edit { preferences ->
                        preferences.remove(stringPreferencesKey("authToken"))
                    }
                }
                _authToken.postValue(null)
                _errorMessage.postValue("Logged out successfully")
                Log.d("AuthViewModel", "User logged out")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout failed: ${e.message}", e)
                _errorMessage.postValue("Failed to sign out")
            }
        }
    }
}
