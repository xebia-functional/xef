package com.xef.xefMobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.server.movile.xef.android.ui.viewmodels.AuthViewModel
import com.xef.xefMobile.services.ApiService
import com.xef.xefMobile.ui.viewmodels.SettingsViewModel

class MainActivity : ComponentActivity() {
  private lateinit var authViewModel: AuthViewModel
  private lateinit var settingsViewModel: SettingsViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    authViewModel = AuthViewModel(this, ApiService())
    settingsViewModel = SettingsViewModel(this)

    // Log out the user when the app starts
    authViewModel.logout()

    setContent {
      XefAndroidApp(authViewModel = authViewModel, settingsViewModel = settingsViewModel)
    }
  }
}
