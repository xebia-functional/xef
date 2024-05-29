package com.xef.xefMobile.ui.viewmodels.factory

import com.server.movile.xef.android.ui.viewmodels.AuthViewModel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.xef.xefMobile.services.ApiService

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context, ApiService()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
