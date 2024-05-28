package com.xef.xefMobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.server.movile.xef.android.ui.viewmodels.AuthViewModel
import com.xef.xefMobile.services.ApiService

class MainActivity : ComponentActivity() {
  private lateinit var authViewModel: AuthViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    authViewModel = AuthViewModel(this, ApiService())

    setContent { XefAndroidApp(authViewModel = authViewModel) }
  }
}
