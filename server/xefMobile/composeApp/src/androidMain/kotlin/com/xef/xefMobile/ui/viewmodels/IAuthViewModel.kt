package com.server.movile.xef.android.ui.viewmodels

import androidx.lifecycle.LiveData

interface IAuthViewModel {
  val authToken: LiveData<String?>
  val isLoading: LiveData<Boolean>
  val errorMessage: LiveData<String?>
  val userName: LiveData<String?>
  val loginError: LiveData<String?> // Add this line

  fun login(email: String, password: String)

  fun register(name: String, email: String, password: String)

  fun logout()
}
