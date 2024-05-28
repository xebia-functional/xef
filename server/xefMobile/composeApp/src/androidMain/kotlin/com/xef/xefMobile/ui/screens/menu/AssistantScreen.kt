package com.server.movile.xef.android.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.server.movile.xef.android.ui.themes.LocalCustomColors
import com.server.movile.xef.android.ui.viewmodels.AssistantViewModel
import com.server.movile.xef.android.ui.viewmodels.IAuthViewModel
import com.xef.xefMobile.ui.screens.Screens
import com.xef.xefMobile.ui.viewmodels.SettingsViewModel

@Composable
fun AssistantScreen(
  navController: NavController,
  authViewModel: IAuthViewModel,
  settingsViewModel: SettingsViewModel
) {
  val customColors = LocalCustomColors.current
  val viewModel: AssistantViewModel =
    viewModel(factory = AssistantViewModelFactory(authViewModel, settingsViewModel))
  val assistants by viewModel.assistants.collectAsState()
  val loading by viewModel.loading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()

  Box(modifier = Modifier.fillMaxSize()) {
    when {
      loading -> {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }
      errorMessage != null -> {
        Text(
          text = errorMessage ?: "",
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.align(Alignment.Center)
        )
      }
      else -> {
        Column(
          modifier = Modifier.align(Alignment.TopCenter).padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Assistants",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
          )
          Divider(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

          Spacer(modifier = Modifier.height(16.dp))

          assistants.forEach { assistant ->
            Text(text = assistant.name, fontWeight = FontWeight.Bold)
            Text(text = assistant.id)
            Spacer(modifier = Modifier.height(8.dp))
          }
        }
      }
    }

    Button(
      onClick = { navController.navigate(Screens.CreateAssistant.screen) },
      colors =
        ButtonDefaults.buttonColors(
          containerColor = customColors.buttonColor,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
      modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
    ) {
      Text(text = "Create New Assistant")
    }
  }
}

class AssistantViewModelFactory(
  private val authViewModel: IAuthViewModel,
  private val settingsViewModel: SettingsViewModel
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST") return AssistantViewModel(authViewModel, settingsViewModel) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
