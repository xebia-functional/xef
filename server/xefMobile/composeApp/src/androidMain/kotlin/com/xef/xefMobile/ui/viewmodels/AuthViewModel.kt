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

    private val _authToken = MutableLiveData<String?>()
    override val authToken: LiveData<String?> = _authToken

    private val _isLoading = MutableLiveData<Boolean>()
    override val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    override val errorMessage: LiveData<String?> = _errorMessage

    private val _userName = MutableLiveData<String?>()
    override val userName: LiveData<String?> = _userName

    init {
        loadAuthToken()
    }

    private fun loadAuthToken() {
        viewModelScope.launch {
            val token = dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("authToken")]
            }.firstOrNull()
            _authToken.value = token


            token?.let {
                loadUserName()
            }
        }
    }

    private suspend fun loadUserName() {
        withContext(Dispatchers.IO) {
            val name = dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("userName")]
            }.firstOrNull()
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
                updateUserName(loginResponse.user.name) // Extract user's name
                _authToken.value = loginResponse.authToken
                _userName.value = loginResponse.user.name
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
                updateUserName(name) // Directly use the name provided during registration
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
        when (e) {
            is IOException -> _errorMessage.postValue("Network error")
            is HttpException -> _errorMessage.postValue("Unexpected server error: ${e.code()}")
            else -> _errorMessage.postValue("An unexpected error occurred: ${e.message}")
        }
    }

    override fun logout() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dataStore.edit { preferences ->
                        preferences.remove(stringPreferencesKey("authToken"))
                        preferences.remove(stringPreferencesKey("userName")) // Add this line
                    }
                }
                _authToken.postValue(null)
                _userName.postValue(null) // Add this line
                _errorMessage.postValue("Logged out successfully")
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to sign out")
            }
        }
    }
}
